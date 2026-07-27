package in.btm.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import in.btm.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PaymentNotReadyException.class)
    public ResponseEntity<ApiResponse<Void>> handlePaymentNotReady(
            PaymentNotReadyException ex,
            HttpServletRequest request) {

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message("PAYMENT_INITIALIZING")
                .error(ex.getMessage())
                .status(HttpStatus.ACCEPTED.value())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntime(
            RuntimeException ex,
            HttpServletRequest request) {

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message("FAILED")
                .error(ex.getMessage())
                .status(HttpStatus.BAD_REQUEST.value())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(
            Exception ex,
            HttpServletRequest request) {

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message("INTERNAL_SERVER_ERROR")
                .error(ex.getMessage())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}