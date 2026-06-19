package in.btm.controller;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import in.btm.dto.ApiResponse;
import in.btm.dto.CartResponse;
import in.btm.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

	private final CartService cartService;

	@PostMapping("/items")
	public ResponseEntity<ApiResponse<CartResponse>> addItem(Authentication authentication,
			@RequestParam Integer productId, @RequestParam Integer quantity, HttpServletRequest request) {

		String email = authentication.getName();

		CartResponse data = cartService.addItem(email, productId, quantity);

		return buildSuccess(data, "Item added to cart", request);
	}

	@GetMapping
	public ResponseEntity<ApiResponse<CartResponse>> getCart(Authentication authentication,
			HttpServletRequest request) {

		String email = authentication.getName();

		return buildSuccess(cartService.getCart(email), "Cart fetched successfully", request);
	}

	@DeleteMapping("/items")
	public ResponseEntity<ApiResponse<CartResponse>> removeItem(Authentication authentication,
			@RequestParam Integer productId, HttpServletRequest request) {

		String email = authentication.getName();

		return buildSuccess(cartService.removeItem(email, productId), "Item removed from cart", request);
	}

	@DeleteMapping
	public ResponseEntity<ApiResponse<Void>> clearCart(Authentication authentication, HttpServletRequest request) {

		String email = authentication.getName();

		cartService.clearCart(email);

		return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Cart cleared").status(200)
				.path(request.getRequestURI()).timestamp(LocalDateTime.now()).build());
	}
	

	private ResponseEntity<ApiResponse<CartResponse>> buildSuccess(CartResponse data, String message,
			HttpServletRequest request) {

		return ResponseEntity.ok(ApiResponse.<CartResponse>builder().success(true).message(message).data(data)
				.status(HttpStatus.OK.value()).path(request.getRequestURI()).timestamp(LocalDateTime.now()).build());
	}
}