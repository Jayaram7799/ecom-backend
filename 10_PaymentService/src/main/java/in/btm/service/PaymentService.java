package in.btm.service;

import in.btm.dto.CreatePaymentRequest;
import in.btm.dto.PaymentVerificationRequest;
import in.btm.dto.RazorpayOrderResponse;

public interface PaymentService {

	RazorpayOrderResponse createPayment(CreatePaymentRequest request);

	void verifyPayment(PaymentVerificationRequest request);

	RazorpayOrderResponse getPaymentByOrderId(Long orderId);
}