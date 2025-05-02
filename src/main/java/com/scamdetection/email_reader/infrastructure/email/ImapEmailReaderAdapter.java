package com.scamdetection.email_reader.infrastructure.email;


import com.scamdetection.email_reader.application.reademail.usecase.ReadEmailsUseCase;
import com.scamdetection.email_reader.config.EmailProperties;
import com.scamdetection.email_reader.domain.EmailMessage;

import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
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

            Message[] messages = inbox.getMessages();

            List<EmailMessage> emailMessages = new ArrayList<>();
            for (Message message : messages) {
                String subject = message.getSubject();
                String from = message.getFrom()[0].toString();
                String body = message.getContent().toString();

                emailMessages.add(new EmailMessage(from, subject, body));
            }

            inbox.close(false);
            store.close();

            readEmailsUseCase.processEmails(emailMessages);

        } catch (Exception e) {
            System.err.println("❌ Error leyendo correos: " + e.getMessage());
        }
    }
}