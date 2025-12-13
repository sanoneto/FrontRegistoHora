package com.aneto.emailservice.service;


import com.aneto.emailservice.dto.request.EmailRequest;

public interface EmailService {
    void sendEmailToGmail(EmailRequest request, String destinationEmail, String subjectPrefix);
    void SendGridServices(EmailRequest request, String destinationEmail, String subjectPrefix);
    void saveEmail(EmailRequest request);
}
