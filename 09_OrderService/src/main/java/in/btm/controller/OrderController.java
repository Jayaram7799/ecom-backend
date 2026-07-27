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
import in.btm.entity.Order;
import in.btm.service.OrderService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;

	@PostMapping
	public ResponseEntity<OrderResponse> createOrder(
	        @RequestBody CreateOrderRequest request,
	        Authentication authentication) {

	    OrderResponse response = orderService.createOrder(request, authentication.getName());

	    return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
	

	@GetMapping("/customer")
	public ResponseEntity<List<Order>> getOrdersByCustomerId(Authentication authentication) {

		return ResponseEntity.ok(orderService.getOrdersByEmail(authentication.getName()));
	}

	@GetMapping
	public ResponseEntity<List<Order>> getAllOrders() {

		return ResponseEntity.ok(orderService.getAllOrders());
	}

	@PutMapping("/{orderId}/cancel")
	public ResponseEntity<String> cancelOrder(@PathVariable Long orderId) {

		orderService.cancelOrder(orderId);

		return ResponseEntity.ok("Order Cancelled Successfully");
	}

	@GetMapping("/{orderId}")
	public ResponseEntity<ApiResponse<OrderDetailsResponse>> getOrderById(@PathVariable Long orderId) {

		return ResponseEntity.ok(

				ApiResponse.<OrderDetailsResponse>builder().success(true).message("Order Details")
						.data(orderService.getOrderDetails(orderId)).build());
	}
}