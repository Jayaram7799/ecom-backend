
package in.btm.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import in.btm.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	// =====================================================
	// Validation Errors
	// =====================================================

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException ex,
			HttpServletRequest request) {

		Map<String, String> errors = new HashMap<>();

		ex.getBindingResult().getAllErrors().forEach(error -> {
			String field = ((FieldError) error).getField();
			String message = error.getDefaultMessage();
			errors.put(field, message);
		});

		return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", errors, request.getRequestURI());
	}

	// =====================================================
	// Cart Exceptions
	// =====================================================

	@ExceptionHandler(CartNotFoundException.class)
	public ResponseEntity<ApiResponse<Object>> handleCartNotFound(CartNotFoundException ex,
			HttpServletRequest request) {

		return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null, request.getRequestURI());
	}

	@ExceptionHandler(CartItemNotFoundException.class)
	public ResponseEntity<ApiResponse<Object>> handleCartItemNotFound(CartItemNotFoundException ex,
			HttpServletRequest request) {

		return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null, request.getRequestURI());
	}

	// =====================================================
	// Product Exceptions
	// =====================================================

	@ExceptionHandler(ProductFetchException.class)
	public ResponseEntity<ApiResponse<Object>> handleProductFetchException(ProductFetchException ex,
			HttpServletRequest request) {

		return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), null, request.getRequestURI());
	}

	@ExceptionHandler(InsufficientStockException.class)
	public ResponseEntity<ApiResponse<Object>> handleInsufficientStock(InsufficientStockException ex,
			HttpServletRequest request) {

		return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null, request.getRequestURI());
	}

	@ExceptionHandler(InvalidQuantityException.class)
	public ResponseEntity<ApiResponse<Object>> handleInvalidQuantity(InvalidQuantityException ex,
			HttpServletRequest request) {

		return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null, request.getRequestURI());
	}

	// =====================================================
	// Security
	// =====================================================

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(AccessDeniedException ex,
			HttpServletRequest request) {

		log.error("Access denied: {}", ex.getMessage());

		return buildResponse(HttpStatus.FORBIDDEN, "Access Denied", ex.getMessage(), request.getRequestURI());
	}

	// =====================================================
	// Illegal Argument
	// =====================================================

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiResponse<Object>> handleIllegalArgument(IllegalArgumentException ex,
			HttpServletRequest request) {

		return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null, request.getRequestURI());
	}

	// =====================================================
	// Catch All
	// =====================================================

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Object>> handleException(Exception ex, HttpServletRequest request) {

		log.error("Unhandled exception", ex);

		return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong", null, request.getRequestURI());
	}

	// =====================================================
	// Common Builder
	// =====================================================

	private ResponseEntity<ApiResponse<Object>> buildResponse(HttpStatus status, String message, Object error,
			String path) {

		return ResponseEntity.status(status).body(ApiResponse.builder().success(false).message(message).error(error)
				.status(status.value()).path(path).timestamp(LocalDateTime.now()).build());
	}
}
