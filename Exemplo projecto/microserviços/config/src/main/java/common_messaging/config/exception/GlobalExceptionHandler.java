package common_messaging.config.exception;


import common_messaging.config.dto.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        log.error("Erro de runtime: {}", e.getMessage(), e);
        ErrorResponse error = new ErrorResponse(
                e.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Argumento inválido: {}", e.getMessage());
        ErrorResponse error = new ErrorResponse(
                e.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.error("Erro de validação: {}", errors);
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e) {
        log.error("Acesso negado: {}", e.getMessage());
        ErrorResponse error = new ErrorResponse(
                "Acesso negado: " + e.getMessage(),
                HttpStatus.FORBIDDEN.value()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        log.error("Erro inesperado: {}", e.getMessage(), e);
        ErrorResponse error = new ErrorResponse(
                "Erro interno do servidor: " + e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }


    @ExceptionHandler(java.net.ConnectException.class)
    public ResponseEntity<ErrorResponse> handleConnectException(java.net.ConnectException e) {
        log.warn("Falha de conexão: {}", e.getMessage());
        ErrorResponse error = new ErrorResponse(
                "Serviço remoto indisponível: " + e.getMessage(),
                HttpStatus.SERVICE_UNAVAILABLE.value()
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(org.springframework.amqp.AmqpConnectException.class)
    public ResponseEntity<ErrorResponse> handleAmqpConnectException(org.springframework.amqp.AmqpConnectException e) {
        log.warn("Falha de conexão com RabbitMQ: {}", e.getMessage());
        ErrorResponse error = new ErrorResponse(
                "Serviço de mensageria indisponível. Verifique se o RabbitMQ está ativo.",
                HttpStatus.SERVICE_UNAVAILABLE.value()
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ErrorResponse> handleResourceAccessException(ResourceAccessException e) {
        log.warn("Erro de acesso a recurso externo: {}", e.getMessage());
        ErrorResponse error = new ErrorResponse(
                "Problema de conectividade com o Vault: " + e.getMessage(),
                HttpStatus.SERVICE_UNAVAILABLE.value()
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException e) {
        log.warn("Falha de autenticação para Users {}", e.getMessage());
        ErrorResponse error = new ErrorResponse(
                "Falha de autenticação para usuário: " + e.getMessage(),
                HttpStatus.UNAUTHORIZED.value()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
    @ExceptionHandler(EmailSendException.class)
    public ResponseEntity<ErrorResponse> handleEmailSendException(EmailSendException e) {
        log.error("Falha ao enviar e-mail: {}", e.getMessage());
        ErrorResponse error = new ErrorResponse(
                "Falha ao enviar e-mail: " + e.getMessage(),
                HttpStatus.SERVICE_UNAVAILABLE.value()
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }
}