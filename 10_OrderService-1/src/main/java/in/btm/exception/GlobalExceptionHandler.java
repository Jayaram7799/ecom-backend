package in.btm.exception;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import in.btm.dto.ApiResponse;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	// ============================================================
	// ORDER NOT FOUND
	// ============================================================

	@ExceptionHandler(OrderNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleOrderNotFound(OrderNotFoundException ex,
			HttpServletRequest request) {

		log.warn("Order not found. path={}, message={}", request.getRequestURI(), ex.getMessage());

		ApiResponse<Void> response = ApiResponse.<Void>builder().success(false).message("Order Not Found")
				.error(ex.getMessage()).status(HttpStatus.NOT_FOUND.value()).path(request.getRequestURI())
				.timestamp(LocalDateTime.now()).build();

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}

	// ============================================================
	// INVALID ORDER
	// ============================================================

	@ExceptionHandler(InvalidOrderException.class)
	public ResponseEntity<ApiResponse<Void>> handleInvalidOrder(InvalidOrderException ex, HttpServletRequest request) {

		log.warn("Invalid order request. path={}, message={}", request.getRequestURI(), ex.getMessage());

		ApiResponse<Void> response = ApiResponse.<Void>builder().success(false).message("Invalid Order")
				.error(ex.getMessage()).status(HttpStatus.BAD_REQUEST.value()).path(request.getRequestURI())
				.timestamp(LocalDateTime.now()).build();

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	// ============================================================
	// REQUEST VALIDATION
	// ============================================================

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex,
			HttpServletRequest request) {

		String errorMessage = ex.getBindingResult().getFieldErrors().stream().map(FieldError::getDefaultMessage)
				.collect(Collectors.joining(", "));

		log.warn("Request validation failed. " + "path={}, errors={}", request.getRequestURI(), errorMessage);

		ApiResponse<Void> response = ApiResponse.<Void>builder().success(false).message("Request Validation Failed")
				.error(errorMessage).status(HttpStatus.BAD_REQUEST.value()).path(request.getRequestURI())
				.timestamp(LocalDateTime.now()).build();

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	// ============================================================
	// CONSTRAINT VALIDATION
	// ============================================================

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex,
			HttpServletRequest request) {

		String errorMessage = ex.getConstraintViolations().stream().map(violation -> violation.getMessage())
				.collect(Collectors.joining(", "));

		log.warn("Constraint validation failed. " + "path={}, errors={}", request.getRequestURI(), errorMessage);

		ApiResponse<Void> response = ApiResponse.<Void>builder().success(false).message("Validation Failed")
				.error(errorMessage).status(HttpStatus.BAD_REQUEST.value()).path(request.getRequestURI())
				.timestamp(LocalDateTime.now()).build();

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	// ============================================================
	// PRODUCT SERVICE - NOT FOUND
	// ============================================================

	@ExceptionHandler(FeignException.NotFound.class)
	public ResponseEntity<ApiResponse<Void>> handleProductNotFound(FeignException.NotFound ex,
			HttpServletRequest request) {

		log.warn("ProductService returned 404. " + "path={}, status={}", request.getRequestURI(), ex.status());

		ApiResponse<Void> response = ApiResponse.<Void>builder().success(false).message("Product Not Found")
				.error("Requested product does not exist").status(HttpStatus.NOT_FOUND.value())
				.path(request.getRequestURI()).timestamp(LocalDateTime.now()).build();

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}

	// ============================================================
	// PRODUCT SERVICE - SERVICE UNAVAILABLE
	// ============================================================

	@ExceptionHandler(FeignException.ServiceUnavailable.class)
	public ResponseEntity<ApiResponse<Void>> handleProductServiceUnavailable(FeignException.ServiceUnavailable ex,
			HttpServletRequest request) {

		log.error("ProductService unavailable. " + "path={}, status={}", request.getRequestURI(), ex.status(), ex);

		ApiResponse<Void> response = ApiResponse.<Void>builder().success(false).message("Product Service Unavailable")
				.error("Unable to communicate with ProductService").status(HttpStatus.SERVICE_UNAVAILABLE.value())
				.path(request.getRequestURI()).timestamp(LocalDateTime.now()).build();

		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
	}

	// ============================================================
	// OTHER FEIGN ERRORS
	// ============================================================

	@ExceptionHandler(FeignException.class)
	public ResponseEntity<ApiResponse<Void>> handleFeignException(FeignException ex, HttpServletRequest request) {

		log.error("Feign communication failure. " + "path={}, status={}", request.getRequestURI(), ex.status(), ex);

		ApiResponse<Void> response = ApiResponse.<Void>builder().success(false).message("Downstream Service Error")
				.error("Unable to communicate with ProductService").status(HttpStatus.BAD_GATEWAY.value())
				.path(request.getRequestURI()).timestamp(LocalDateTime.now()).build();

		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
	}

	// ============================================================
	// DATABASE EXCEPTION
	// ============================================================

	@ExceptionHandler(DataAccessException.class)
	public ResponseEntity<ApiResponse<Void>> handleDatabaseException(DataAccessException ex,
			HttpServletRequest request) {

		log.error("Database operation failed. path={}", request.getRequestURI(), ex);

		ApiResponse<Void> response = ApiResponse.<Void>builder().success(false).message("Database Error")
				.error("Unable to process the request").status(HttpStatus.INTERNAL_SERVER_ERROR.value())
				.path(request.getRequestURI()).timestamp(LocalDateTime.now()).build();

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}

	// ============================================================
	// UNEXPECTED EXCEPTION
	// ============================================================

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleException(Exception ex, HttpServletRequest request) {

		/*
		 * NEVER expose ex.getMessage() to the client for unexpected exceptions.
		 *
		 * It can expose:
		 *
		 * - SQL information - database details - internal class names - infrastructure
		 * details - Feign request details
		 */
		log.error("Unexpected error. path={}", request.getRequestURI(), ex);

		ApiResponse<Void> response = ApiResponse.<Void>builder().success(false).message("Internal Server Error")
				.error("An unexpected error occurred").status(HttpStatus.INTERNAL_SERVER_ERROR.value())
				.path(request.getRequestURI()).timestamp(LocalDateTime.now()).build();

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}
}