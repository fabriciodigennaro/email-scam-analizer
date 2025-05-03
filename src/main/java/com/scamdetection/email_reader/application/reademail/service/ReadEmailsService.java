package com.scamdetection.email_reader.application.reademail.service;

import com.scamdetection.email_reader.application.reademail.usecase.ReadEmailsUseCase;
import com.scamdetection.email_reader.domain.EmailMessage;

import java.util.List;

public class ReadEmailsService implements ReadEmailsUseCase {
    @Override
    public void processEmails(List<EmailMessage> emails) {
        for (EmailMessage email : emails) {
            System.out.println("Procesando subject: " + email.subject());
            System.out.println("Procesando body: " + email.body());
            System.out.println("Procesando sender: " + email.from());

        }
    }
}
