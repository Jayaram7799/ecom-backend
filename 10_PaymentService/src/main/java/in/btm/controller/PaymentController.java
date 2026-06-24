package in.btm.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import in.btm.dto.CreatePaymentRequest;
import in.btm.dto.PaymentVerificationRequest;
import in.btm.dto.RazorpayOrderResponse;
import in.btm.service.PaymentService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentService paymentService;

	@PostMapping("/create")
	public ResponseEntity<RazorpayOrderResponse> createPayment(@RequestBody CreatePaymentRequest request) {

		return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createPayment(request));
	}

	@GetMapping("/order/{orderId}")
	public ResponseEntity<RazorpayOrderResponse> getPaymentByOrderId(@PathVariable Long orderId) {

		return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
	}

	@PostMapping("/verify")
	public ResponseEntity<String> verifyPayment(@RequestBody PaymentVerificationRequest request) {

		paymentService.verifyPayment(request);

		return ResponseEntity.ok("Payment Verified Successfully");
	}
}