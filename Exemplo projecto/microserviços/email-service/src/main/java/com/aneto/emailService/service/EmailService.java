package com.aneto.emailService.service;

import com.aneto.emailService.dto.request.EmailRequest;

public interface EmailService {
    void sendEmail(EmailRequest request);
    void saveEmail(EmailRequest request);
}