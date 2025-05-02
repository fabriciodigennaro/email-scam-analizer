package com.scamdetection.email_reader.domain;

public record EmailMessage(String from, String subject, String body) {}
