// language: java
package com.aneto.emailservice.queue;

import com.aneto.emailservice.config.RabbitMQConfig;
import com.aneto.emailservice.dto.request.EmailRequest;
import com.aneto.emailservice.service.EmailService;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailQueueListener {

    private static final Logger log = LoggerFactory.getLogger(EmailQueueListener.class);
    private final EmailService emailService;

    // Configurações de destino e prefixo do Email Service (assumimos valores padrão para a fila)
    // Estes valores devem ser injetados ou definidos por convenção se a fila for genérica.
    private static final String DEFAULT_DESTINATION = "admin@aneto.com"; // Assuma valor do application.yml
    private static final String DEFAULT_PREFIX = "Mensagem Assíncrona";


    @RabbitListener(
            queues = RabbitMQConfig.EMAIL_QUEUE,
            messageConverter = "rabbitJsonConverter"
    )
    public void handleEmailRequest(EmailRequest request) {

        log.info("Mensagem recebida da fila '{}': {}", RabbitMQConfig.EMAIL_QUEUE, request);

        try {
            // O Email Service irá agora enviar o email com a lógica existente
            emailService.sendEmailToGmail(request, DEFAULT_DESTINATION, DEFAULT_PREFIX);
            log.info("Email enviado com sucesso para o destino padrão.");

           // emailService.SendGridServices(request, DEFAULT_DESTINATION, DEFAULT_PREFIX);

        } catch (Exception e) {
            // Em um ambiente de produção, logar o erro e considerar re-enviar (NACK) ou mover para uma DLQ (Dead Letter Queue)
            log.error("Falha ao processar e enviar Gmail da fila para {}: {}", request.email(), e.getMessage());
            log.info("vamos tenatr enviar por sendGrid .");
            // Se esta exceção for lançada, o RabbitMQ tentará reprocessar a mensagem (comportamento padrão)
            throw new RuntimeException("Erro de processamento da fila, re-entrega será tentada.", e);
        }
    }
}