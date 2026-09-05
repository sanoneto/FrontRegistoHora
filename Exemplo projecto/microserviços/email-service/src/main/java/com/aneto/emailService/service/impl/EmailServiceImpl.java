package com.aneto.emailService.service.impl;

import com.aneto.emailService.dto.request.EmailRequest;
import com.aneto.emailService.exception.EmailSendException;
import com.aneto.emailService.mapper.EmailMapper;
import com.aneto.emailService.models.Emails;
import com.aneto.emailService.repository.EmailRepository;
import com.aneto.emailService.service.EmailService;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final EmailRepository emailRepository;
    private final Resend resend;
    private final EmailMapper emailMapper;

    @Value("${email.from}")
    private String defaultFrom;

    @Value("${email.to}")
    private String defaultTo;

    @Value("${email.subject.prefix}")
    private String subjectPrefix;

    @Override
    @Transactional
    public void sendEmail(EmailRequest request) {
        log.info("Iniciando processo de envio de e-mail para: {}", request.to());

        String subject = StringUtils.hasText(request.subject())
                ? request.subject()
                : subjectPrefix + " - Nova Mensagem de Contacto";

        String var_to = StringUtils.hasText(request.to())
                ? request.to()
                : defaultTo;

        CreateEmailOptions options = CreateEmailOptions.builder()
                .from(defaultFrom)
                .to(var_to)
                .subject(subject)
                .html(buildHtmlBody(request))
                .build();

        try {
            // Chamada à API do Resend
            CreateEmailResponse response = resend.emails().send(options);

            if (response != null && response.getId() != null) {
                log.info("E-mail enviado com sucesso via Resend! ID: {}", response.getId());
                // Só salvamos no banco se a API confirmar o envio
                this.saveEmail(request);
            } else {
                log.warn("Resend retornou resposta vazia para o e-mail: {}", var_to);
            }

        } catch (ResendException e) {
            log.error("Erro de negócio na API Resend: {}", e.getMessage());
            throw new EmailSendException("Falha técnica ao enviar e-mail via Resend.");

        } catch (RuntimeException e) {
            // Captura o erro 429 (Rate Limit) ou outros erros de runtime do SDK
            log.error("Erro de execução (Rate Limit?) ao enviar e-mail: {}", e.getMessage());

            // Relançamos para que o RabbitMQ possa realizar o retry (Backoff)
            throw e;

        } catch (Exception e) {
            log.error("Erro inesperado no serviço de e-mail: {}", e.getClass().getName());
            throw new EmailSendException("Erro interno no processamento de e-mail.");
        }
    }

    @Override
    @Transactional
    public void saveEmail(EmailRequest request) {
        try {
            Emails emailEntity = emailMapper.toEntity(request);
            emailRepository.save(emailEntity);
            log.debug("Dados do e-mail persistidos no banco de dados.");
        } catch (Exception e) {
            log.error("Erro ao persistir log de e-mail no banco: {}", e.getMessage());
            // Dependendo da criticidade, você pode ou não lançar exceção aqui
        }
    }

    private String buildHtmlBody(EmailRequest request) {
        String resetButton = "";

        if (StringUtils.hasText(request.resetLink())) {
            resetButton = """
                <div style='text-align: center; margin: 25px 0;'>
                    <a href='%s' style='background-color: #4F46E5; color: #ffffff; padding: 12px 25px; text-decoration: none; border-radius: 6px; font-weight: bold; display: inline-block;'>
                        Redefinir Minha Senha
                    </a>
                </div>
                """.formatted(request.resetLink());
        }

        return """
            <div style='font-family: "Segoe UI", Helvetica, Arial, sans-serif; color: #333; line-height: 1.6; max-width: 600px; margin: 0 auto; border: 1px solid #e5e7eb; border-radius: 8px; overflow: hidden;'>
                <div style='background-color: #4F46E5; padding: 20px; text-align: center;'>
                    <h1 style='color: #ffffff; margin: 0; font-size: 24px;'>ProAct System</h1>
                </div>
                
                <div style='padding: 30px 20px;'>
                    <p style='font-size: 16px;'>Olá, <strong>Administrador</strong>,</p>
                    <p>Você recebeu uma nova mensagem de <strong>%s</strong> através do formulário de contato:</p>
                    
                    <div style='background-color: #f3f4f6; border-left: 4px solid #4F46E5; padding: 20px; margin: 20px 0; border-radius: 4px;'>
                        <p style='margin: 0; font-style: italic; color: #374151; white-space: pre-wrap;'>"%s"</p>
                    </div>
                    
                    %s
                    
                    <p style='font-size: 13px; color: #6b7280; margin-top: 20px;'>
                        Este contato foi enviado para o e-mail: <strong>%s</strong>
                    </p>
                </div>
                
                <div style='background-color: #f9fafb; padding: 20px; border-top: 1px solid #e5e7eb; font-size: 11px; color: #9ca3af; text-align: center;'>
                    <p style='margin: 5px 0;'><strong>ProAct (Time & Resource Efficiency Group), Lda</strong></p>
                    <p style='margin: 5px 0;'>Lisboa, Portugal</p>
                    <p style='margin: 15px 0;'>© 2026 ProAct System. Todos os direitos reservados.</p>
                </div>
            </div>
            """.formatted(
                request.name(),
                request.message(),
                resetButton,
                request.from()
        );
    }
}