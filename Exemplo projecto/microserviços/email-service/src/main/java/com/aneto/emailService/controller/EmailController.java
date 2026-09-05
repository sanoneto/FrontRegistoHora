package com.aneto.emailService.controller;

import com.aneto.emailService.dto.request.EmailRequest;
import com.aneto.emailService.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @Value("${email.to}")
    private String defaultTo;

    @Value("${email.subject.prefix:Contato}")
    private String defaultSubject;

    @PostMapping("/send")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ESTAGIARIO')")
    public ResponseEntity<String> sendEmail(@RequestBody @Valid EmailRequest request) {

        log.info("Recebida requisição de e-mail de: {}", request.from());

        EmailRequest processedRequest = resolveEmailData(request);

        emailService.sendEmail(processedRequest);

        log.info("E-mail enviado com sucesso para: {}", processedRequest.to());

        return ResponseEntity.ok("E-mail enviado com sucesso");
    }

    /**
     * Aplica a lógica de fallback:
     * 1. Valor do JSON (se preenchido)
     * 2. Valor padrão do application.yml (se o JSON estiver em branco)
     */
    private EmailRequest resolveEmailData(EmailRequest request) {
        // Usa o valor do request. Se estiver em branco (null ou ""), usa o default
        String finalTo = StringUtils.hasText(request.to())
                ? request.to()
                : defaultTo;

        String finalSubject = StringUtils.hasText(request.subject())
                ? request.subject()
                : defaultSubject;

        return new EmailRequest(
                request.name(),
                request.from(),
                finalTo,
                finalSubject,
                request.message(),
                request.resetLink()
        );
    }
}