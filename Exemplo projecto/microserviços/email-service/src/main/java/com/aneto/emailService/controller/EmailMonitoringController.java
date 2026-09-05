package com.aneto.emailService.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/monitoring/email")
@PreAuthorize("hasRole('ADMIN')") // Garante que apenas ADMINs vejam logs
public class EmailMonitoringController {

    // Deve ser o mesmo caminho configurado no logback-spring.xml
    private static final String LOG_FILE_PATH = "logs/error.log";

    /**
     * Endpoint para buscar as linhas do arquivo de log.
     * Retorna uma lista de Strings (cada linha do arquivo é um elemento).
     */
    @GetMapping("/logs")
    public ResponseEntity<List<String>> getEmailLogs() {
        log.info("Requisição para ler logs de e-mail.");
        Path path = Paths.get(LOG_FILE_PATH);

        if (!Files.exists(path)) {
            log.warn("Arquivo de log não encontrado: {}", LOG_FILE_PATH);
            return ResponseEntity.ok(Collections.emptyList());
        }

        try (Stream<String> lines = Files.lines(path)) {
            // Lê o arquivo, coleta as linhas e inverte para o mais recente aparecer primeiro
            List<String> logLines = lines.collect(Collectors.toList());
            Collections.reverse(logLines);
            return ResponseEntity.ok(logLines);
        } catch (IOException e) {
            log.error("Erro ao ler o arquivo de log", e);
            return ResponseEntity.internalServerError().body(null);
        }
    }

    /**
     * Endpoint para limpar o arquivo de log (truncar o arquivo).
     */
    @DeleteMapping("/logs")
    public ResponseEntity<Void> clearEmailLogs() {
        log.info("Requisição para limpar logs de e-mail.");
        Path path = Paths.get(LOG_FILE_PATH);

        try {
            if (Files.exists(path)) {
                // Escreve um array de bytes vazio para truncar o arquivo
                Files.write(path, new byte[0]);
                log.info("Logs de e-mail limpos com sucesso.");
            }
            return ResponseEntity.noContent().build();
        } catch (IOException e) {
            log.error("Erro ao limpar o arquivo de log", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}