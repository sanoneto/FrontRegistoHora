package com.aneto.emailService.queue;

import com.aneto.emailService.config.RabbitMQConfig;
import com.aneto.emailService.dto.request.EmailRequest;
import com.aneto.emailService.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailQueueListener {

    private final EmailService emailService;

    @Value("${email.from}")
    private String defaultFrom;

    @Value("${email.subject.prefix:Contato}")
    private String defaultSubject;

    @RabbitListener(
            queues = RabbitMQConfig.EMAIL_QUEUE,
            messageConverter = "rabbitJsonConverter"
    )
    public void handleEmailRequest(EmailRequest request) {
        // Garantimos que o log não quebre se o request vier vazio
        String targetIdentifier = (request != null && request.from() != null) ? request.from() : "Desconhecido";

        log.info("Recebendo mensagem da fila '{}' para o e-mail: {}", RabbitMQConfig.EMAIL_QUEUE, targetIdentifier);

        try {
            if (request == null) {
                log.warn("Recebido corpo de mensagem nulo da fila. Ignorando processamento.");
                return;
            }

            EmailRequest consolidatedRequest = consolidateRequest(request);
            emailService.sendEmail(consolidatedRequest);

            log.info("E-mail para {} processado com sucesso via fila.", consolidatedRequest.from());

        } catch (Exception e) {
            log.error("Falha crítica ao processar fila para {}: {}", targetIdentifier, e.getMessage());
            // Mantendo a RuntimeException para trigger de retry do RabbitMQ
            throw new RuntimeException("Erro no processamento da fila - disparando política de re-entrega", e);
        }
    }

    /**
     * Aplica a lógica de fallback: Parâmetros do Request > Valores do Application.yml
     */
    private EmailRequest consolidateRequest(EmailRequest request) {
        log.info("Consolidando dados do Request para envio de e-mail.{}",request.toString());
        String finalSubject = isBlank(request.subject()) ? defaultSubject : request.subject();
        return new EmailRequest(
                request.name(),
                defaultFrom,
                request.to(),
                finalSubject,
                request.message(),
                request.resetLink()
        );
    }

    private boolean isBlank(String str) {
        return str == null || str.isBlank();
    }
}