package in.btm.exceptions;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import in.btm.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
          .getFieldErrors()
          .forEach(error ->
                  errors.put(error.getField(),
                             error.getDefaultMessage()));

        ApiResponse<Map<String, String>> response =
                ApiResponse.<Map<String, String>>builder()
                        .success(false)
                        .message("Validation failed")
                        .data(errors)
                        .status(HttpStatus.BAD_REQUEST.value())
                        .path(request.getRequestURI())
                        .timestamp(LocalDateTime.now())
                        .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleEmailAlreadyExists(
            EmailAlreadyExistsException ex,
            HttpServletRequest request) {

        return buildErrorResponse(
                ex.getMessage(),
                HttpStatus.CONFLICT,
                request);
    }

    @ExceptionHandler(EmailNotRegisteredException.class)
    public ResponseEntity<ApiResponse<Object>> handleEmailNotRegistered(
            EmailNotRegisteredException ex,
            HttpServletRequest request) {

        return buildErrorResponse(
                ex.getMessage(),
                HttpStatus.NOT_FOUND,
                request);
    }

    @ExceptionHandler(AccountNotActivatedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccountNotActivated(
            AccountNotActivatedException ex,
            HttpServletRequest request) {

        return buildErrorResponse(
                ex.getMessage(),
                HttpStatus.FORBIDDEN,
                request);
    }

    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handlePasswordMismatch(
            PasswordMismatchException ex,
            HttpServletRequest request) {

        return buildErrorResponse(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST,
                request);
    }

    @ExceptionHandler(InvalidTemporaryPasswordException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidTemporaryPassword(
            InvalidTemporaryPasswordException ex,
            HttpServletRequest request) {

        return buildErrorResponse(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST,
                request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGlobalException(
            Exception ex,
            HttpServletRequest request) {

        return buildErrorResponse(
                ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                request);
    }

    private ResponseEntity<ApiResponse<Object>> buildErrorResponse(
            String message,
            HttpStatus status,
            HttpServletRequest request) {

        ApiResponse<Object> response =
                ApiResponse.builder()
                        .success(false)
                        .message(message)
                        .data(null)
                        .status(status.value())
                        .path(request.getRequestURI())
                        .timestamp(LocalDateTime.now())
                        .build();

        return ResponseEntity.status(status).body(response);
    }
}