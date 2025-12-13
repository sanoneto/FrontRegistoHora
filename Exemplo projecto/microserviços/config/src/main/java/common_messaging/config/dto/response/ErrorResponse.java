package common_messaging.config.dto.response;

public record ErrorResponse(
        String message,
        int status
) {
}