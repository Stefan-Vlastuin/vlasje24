package nl.vlasje24.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage()));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", "INVALID_REQUEST");
        body.put("message", "Controleer de ingevulde gegevens");
        body.put("fieldErrors", fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(InvalidAccountDataException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidAccountData(InvalidAccountDataException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", "INVALID_REQUEST");
        body.put("message", ex.getMessage());
        body.put("fieldErrors", Map.of("password", ex.getMessage()));
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(AccountAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleAccountExists(AccountAlreadyExistsException ex) {
        return error(HttpStatus.CONFLICT, "ACCOUNT_EXISTS", ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return error(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", ex.getMessage());
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<Map<String, Object>> handleTooManyRequests(
            TooManyRequestsException ex,
            HttpServletRequest request) {
        log.warn("Rate limit bereikt (methode={}, pad={})",
                request.getMethod(), request.getRequestURI());
        return error(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMITED", ex.getMessage());
    }

    @ExceptionHandler(ModerationConflictException.class)
    public ResponseEntity<Map<String, Object>> handleModerationConflict(ModerationConflictException ex) {
        return error(HttpStatus.CONFLICT, "MODERATION_CONFLICT", ex.getMessage());
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<Map<String, Object>> handleEmailNotVerified(EmailNotVerifiedException ex) {
        return error(HttpStatus.FORBIDDEN, "EMAIL_NOT_VERIFIED", ex.getMessage());
    }

    @ExceptionHandler(CommentAccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleCommentAccessDenied(CommentAccessDeniedException ex) {
        return error(HttpStatus.FORBIDDEN, "FORBIDDEN", ex.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Ongeldige aanvraag");
    }

    @ExceptionHandler(InvalidAccountTokenException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidAccountToken(InvalidAccountTokenException ex) {
        if (ex.isAlreadyUsed()) {
            return error(HttpStatus.CONFLICT, "TOKEN_USED", ex.getMessage());
        }
        return error(HttpStatus.BAD_REQUEST, "INVALID_TOKEN", ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(Map.of("code", code, "message", message));
    }
}
