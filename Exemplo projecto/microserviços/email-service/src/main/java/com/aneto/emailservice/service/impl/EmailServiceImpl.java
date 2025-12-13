package com.aneto.emailservice.service.impl;


import com.aneto.emailservice.dto.request.EmailRequest;
import com.aneto.emailservice.exception.EmailSendException;
import com.aneto.emailservice.models.Emails;
import com.aneto.emailservice.repository.EmailRepository;
import com.aneto.emailservice.service.EmailService;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender javaMailSender;
    private final EmailRepository emailRepository;

    @Value("${email.from}")
    private String defaultFrom;

    @Value("${EMAIL_TO}")
    private String defaultTo;

    @Value("${sendgrid.api-key}")
    private String sendGridApiKey;

    @Override
    public void sendEmailToGmail(EmailRequest request,
                                 String destinationEmail,
                                 String subjectPrefix) {

        String subject = buildSubject(subjectPrefix, request.name(), false);
        String body = buildBody(request);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(request.email());
        message.setSubject(request.subject());
        message.setText(body);

        log.info("A enviar o email para {} com assunto '{}'", request.email(), subject);
        try {
            javaMailSender.send(message);
            log.info("Email enviado com sucesso para {}", request.email());

            saveEmail(request);
        } catch (MailAuthenticationException e) {
            throw new EmailSendException(
                    "Falha de autenticação no servidor SMTP. " +
                            "Verifique usuário/senha (MAIL_USERNAME/MAIL_PASSWORD) e configurações de SMTP."
            );
        } catch (MailException e) {
            throw new EmailSendException("Erro ao enviar email via servidor SMTP.");
        }
    }

    @Override
    public void SendGridServices(EmailRequest request,
                                 String destinationEmail,
                                 String subjectPrefix) {
        validateSendGridApiKey();

        String subject = request.subject() ;

        String contentText = buildBody(request);

        Email from = new Email(defaultFrom);
        Email to = new Email(defaultTo);
        Content content = new Content("text/plain", request.message());

        Mail mail = new Mail(from, subject, to, content);

        SendGrid sendGrid = new SendGrid(sendGridApiKey);
        Request sgRequest = new Request();

        try {
            sgRequest.setMethod(Method.POST);
            sgRequest.setEndpoint("mail/send");
            sgRequest.setBody(mail.build());


            log.info("Enviando email via SendGrid para {} com assunto '{}'", defaultTo, subject);
            Response response = sendGrid.api(sgRequest);

            //saveEmail(request);

            log.info("SendGrid - status: {}, body: {}, headers: {}",
                    response.getStatusCode(),
                    response.getBody(),
                    response.getHeaders());
        } catch (IOException ex) {
            log.error("Erro ao enviar email via SendGrid para {}", defaultTo);
            throw new IllegalStateException("Erro ao enviar email via SendGrid ", ex);
        }
    }

    @Override
    public void saveEmail(EmailRequest request) {
        log.info("A guardar email para base dados");

        emailRepository.save(new Emails(request.name(), request.email(), request.resetLink()));
        log.info("O email guardado com sucesso para base dados");
    }

    private String buildSubject(String subjectPrefix, String senderName, boolean useDashSeparator) {
        if (subjectPrefix == null || subjectPrefix.isBlank()) {
            return "Mensagem de " + senderName;
        }
        String separator = useDashSeparator ? " - " : " ";
        return subjectPrefix.trim() + separator + "Mensagem de " + senderName;
    }

    private String buildBody(EmailRequest request) {
        return "Nome: " + request.name() + "\n" +
                "Email: " + request.email() + "\n\n" +
                "Mensagem:\n" + request.message() + "\n";
    }

    private void validateSendGridApiKey() {
        log.info("Email sendGridApiKey : {}", sendGridApiKey);
        if (sendGridApiKey == null || sendGridApiKey.isBlank()) {
            log.error("SendGrid API key não configurada. Verifique a propriedade 'sendgrid.api-key'.");
            throw new IllegalStateException("SendGrid API key não configurada");
        }
    }
}