package in.btm.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import in.btm.dto.ApiResponse;
import in.btm.dto.CreateOrderRequest;
import in.btm.dto.OrderDetailsResponse;
import in.btm.dto.OrderResponse;
import in.btm.dto.OrderSummaryResponse;
import in.btm.entity.Order;
import in.btm.service.OrderService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;

	// ============================================================
	// CREATE ORDER
	// ============================================================

	@PostMapping
	public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody CreateOrderRequest request,
			Authentication authentication) {

		String email = authentication.getName();

		OrderResponse orderResponse = orderService.createOrder(request, email);

		ApiResponse<OrderResponse> response = ApiResponse.<OrderResponse>builder().success(true)
				.message("Order created successfully").data(orderResponse).build();

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	// ============================================================
	// GET CUSTOMER ORDERS
	// ============================================================

	@GetMapping("/customer")
	public ResponseEntity<ApiResponse<List<OrderSummaryResponse>>> getOrdersByCustomer(Authentication authentication) {

		String email = authentication.getName();

		List<OrderSummaryResponse> orders = orderService.getOrdersByEmail(email);

		ApiResponse<List<OrderSummaryResponse>> response = ApiResponse.<List<OrderSummaryResponse>>builder()
				.success(true).message("Customer orders retrieved successfully").data(orders)
				.status(HttpStatus.OK.value()).build();

		return ResponseEntity.ok(response);
	}

	// ============================================================
	// GET ALL ORDERS
	// ============================================================

	@GetMapping
	public ResponseEntity<ApiResponse<List<Order>>> getAllOrders() {

		List<Order> orders = orderService.getAllOrders();

		ApiResponse<List<Order>> response = ApiResponse.<List<Order>>builder().success(true)
				.message("Orders retrieved successfully").data(orders).build();

		return ResponseEntity.ok(response);
	}

	// ============================================================
	// GET ORDER DETAILS
	// ============================================================

	@GetMapping("/{orderId}")
	public ResponseEntity<ApiResponse<OrderDetailsResponse>> getOrderById(@PathVariable Long orderId,
			Authentication authentication) {

		String email = authentication.getName();

		OrderDetailsResponse details = orderService.getOrderDetails(orderId, email);

		ApiResponse<OrderDetailsResponse> response = ApiResponse.<OrderDetailsResponse>builder().success(true)
				.message("Order details retrieved successfully").data(details).build();

		return ResponseEntity.ok(response);
	}

	// ============================================================
	// CANCEL ORDER
	// ============================================================

	@PutMapping("/{orderId}/cancel")
	public ResponseEntity<ApiResponse<Void>> cancelOrder(@PathVariable Long orderId, Authentication authentication) {

		String email = authentication.getName();

		orderService.cancelOrder(orderId, email);

		ApiResponse<Void> response = ApiResponse.<Void>builder().success(true).message("Order cancelled successfully")
				.build();

		return ResponseEntity.ok(response);
	}
}