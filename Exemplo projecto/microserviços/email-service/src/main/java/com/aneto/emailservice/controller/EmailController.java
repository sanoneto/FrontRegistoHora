package com.aneto.emailservice.controller;



import com.aneto.emailservice.dto.request.EmailRequest;
import com.aneto.emailservice.service.EmailService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @Value("${email.to}")
    private String defaultEmailDestination;

    @Value("${email.subject.prefix:Contato}")
    private String defaultSubjectPrefix;

    private static final Logger log = LoggerFactory.getLogger(EmailController.class);

    // Endpoint 1: SendGrid
    @PostMapping("/sendGrid")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ESTAGIARIO')") // Protegido!
    public ResponseEntity<String> SendGridServices(@RequestBody @Valid EmailRequest request,
                                                   @RequestParam(required = false) String destination,
                                                   @RequestParam(required = false) String subjectPrefix) {


        Result result = getResult(request, destination, subjectPrefix);

        emailService.SendGridServices(request, result.finalDestination(), result.finalSubjectPrefix());

        return ResponseEntity.ok("Email enviado com sucesso");
    }

    // Endpoint 2: Gmail
    @PostMapping("/sendGmail")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ESTAGIARIO')") // Protegido!
    public ResponseEntity<String> sendEmailToGmail(@RequestBody @Valid EmailRequest request,
                                                   @RequestParam(required = false) String destination,
                                                   @RequestParam(required = false) String subjectPrefix) {

        Result result = getResult(request, destination, subjectPrefix);

        emailService.sendEmailToGmail(request, result.finalDestination(), result.finalSubjectPrefix());

        return ResponseEntity.ok("Email enviado com sucesso");
    }

    // ... (Métodos auxiliares) ...
    private Result getResult(EmailRequest request, String destination, String subjectPrefix) {
        // ... (lógica de destino e prefixo)
        // Usando as classes internas ou DTOs que você já tem
        String finalDestination = (destination != null && !destination.isEmpty())
                ? destination
                : defaultEmailDestination;

        String finalSubjectPrefix = (subjectPrefix != null && !subjectPrefix.isEmpty())
                ? subjectPrefix
                : defaultSubjectPrefix;

        log.info("Recebida requisição de envio de e-mail: {}, destino: {}", request, finalDestination);
        Result result = new Result(finalDestination, finalSubjectPrefix);
        return result;
    }

    private record Result(String finalDestination, String finalSubjectPrefix) {
    }
}