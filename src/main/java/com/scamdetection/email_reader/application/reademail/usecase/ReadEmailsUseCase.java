package com.scamdetection.email_reader.application.reademail.usecase;

import com.scamdetection.email_reader.domain.EmailMessage;

import java.util.List;

public interface ReadEmailsUseCase {
    void processEmails(List<EmailMessage> emails);
}
