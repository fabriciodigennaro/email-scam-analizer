package com.scamdetection.email_reader.config;

import com.scamdetection.email_reader.application.reademail.service.ReadEmailsService;
import com.scamdetection.email_reader.application.reademail.usecase.ReadEmailsUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {
    @Bean
    public ReadEmailsUseCase readEmailsUseCase() {
        return new ReadEmailsService();
    }
}
