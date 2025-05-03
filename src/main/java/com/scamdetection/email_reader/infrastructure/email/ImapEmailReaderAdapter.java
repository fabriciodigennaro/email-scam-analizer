package com.scamdetection.email_reader.infrastructure.email;


import com.scamdetection.email_reader.application.reademail.usecase.ReadEmailsUseCase;
import com.scamdetection.email_reader.config.EmailProperties;
import com.scamdetection.email_reader.domain.EmailMessage;

import jakarta.mail.Flags;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.search.FlagTerm;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

@Component
public class ImapEmailReaderAdapter {

    private final EmailProperties properties;
    private final ReadEmailsUseCase readEmailsUseCase;

    public ImapEmailReaderAdapter(EmailProperties properties, ReadEmailsUseCase useCase) {
        this.properties = properties;
        this.readEmailsUseCase = useCase;
    }

    @Scheduled(fixedDelayString = "#{${email.check-interval} * 1000}")
    public void fetchAndProcessEmails() {
        try {
            Properties props = new Properties();
            props.put("mail.store.protocol", properties.getProtocol());

            Session session = Session.getInstance(props);
            Store store = session.getStore();
            store.connect(properties.getHost(), properties.getUsername(), properties.getPassword());

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));

            List<EmailMessage> emailMessages = new ArrayList<>();
            for (Message message : messages) {
                String subject = message.getSubject();
                String from = message.getFrom()[0].toString();
                String body = getTextFromMessage(message);;

                emailMessages.add(new EmailMessage(from, subject, body));
            }

            inbox.close(false);
            store.close();

            readEmailsUseCase.processEmails(emailMessages);

        } catch (Exception e) {
            System.err.println("❌ Error leyendo correos: " + e.getMessage());
        }
    }

    private String getTextFromMessage(Message message) throws Exception {
        Object content = message.getContent();

        if (content instanceof String) {
            return (String) content;
        }

        if (content instanceof MimeMultipart) {
            return getTextFromMimeMultipart((MimeMultipart) content);
        }

        return "";
    }

    private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws Exception {
        String text = null;
        String html = null;

        for (int i = 0; i < mimeMultipart.getCount(); i++) {
            var bodyPart = mimeMultipart.getBodyPart(i);

            if (bodyPart.isMimeType("text/plain") && text == null) {
                text = bodyPart.getContent().toString();
            } else if (bodyPart.isMimeType("text/html") && html == null) {
                html = bodyPart.getContent().toString();
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                String nested = getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
                if (text == null) text = nested;
            }
        }

        if (html != null) {
            return org.jsoup.Jsoup.parse(html).text();
        } else return Objects.requireNonNullElse(text, "");
    }
}