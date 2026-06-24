package in.btm.service;

import java.math.BigDecimal;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;

import in.btm.config.RazorpayConfig;
import in.btm.dto.CreatePaymentRequest;
import in.btm.dto.PaymentVerificationRequest;
import in.btm.dto.RazorpayOrderResponse;
import in.btm.entity.Payment;
import in.btm.enums.PaymentStatus;
import in.btm.kafka.event.PaymentFailedEvent;
import in.btm.kafka.event.PaymentInitiatedEvent;
import in.btm.kafka.event.PaymentSuccessEvent;
import in.btm.kafka.producer.PaymentFailedProducer;
import in.btm.kafka.producer.PaymentInitiatedProducer;
import in.btm.kafka.producer.PaymentSuccessProducer;
import in.btm.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

	private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

	private final PaymentRepository paymentRepository;

	private final RazorpayConfig razorpayConfig;

	private final PaymentInitiatedProducer paymentInitiatedProducer;

	private final PaymentSuccessProducer paymentSuccessProducer;

	private final PaymentFailedProducer paymentFailedProducer;

	@Override
	public RazorpayOrderResponse createPayment(CreatePaymentRequest request) {

		try {

			log.info("Creating payment for orderId={}", request.getOrderId());

			RazorpayClient client = new RazorpayClient(razorpayConfig.getKeyId(), razorpayConfig.getKeySecret());

			JSONObject options = new JSONObject();

			options.put("amount", request.getAmount().multiply(BigDecimal.valueOf(100)).intValue());

			options.put("currency", "INR");

			options.put("receipt", "order_" + request.getOrderId());

			Order razorpayOrder = client.orders.create(options);

			Payment payment = new Payment();

			payment.setOrderId(request.getOrderId());

			payment.setCustomerId(request.getCustomerId());

			payment.setAmount(request.getAmount());

			payment.setRazorpayOrderId(razorpayOrder.get("id"));

			payment.setPaymentStatus(PaymentStatus.PENDING);

			payment = paymentRepository.save(payment);

			paymentInitiatedProducer.publish(

					PaymentInitiatedEvent.builder().orderId(payment.getOrderId())
							.razorpayOrderId(payment.getRazorpayOrderId()).amount(payment.getAmount()).build());

			log.info("Payment created successfully for orderId={}", payment.getOrderId());

			return RazorpayOrderResponse.builder().razorpayOrderId(payment.getRazorpayOrderId())
					.amount(payment.getAmount()).currency("INR").key(razorpayConfig.getKeyId()).build();

		} catch (Exception ex) {

			log.error("Payment creation failed", ex);

			paymentFailedProducer.publish(

					PaymentFailedEvent.builder().orderId(request.getOrderId()).reason(ex.getMessage()).build());

			throw new RuntimeException("Unable to create payment");
		}
	}

	@Override
	public void verifyPayment(PaymentVerificationRequest request) {

		Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
				.orElseThrow(() -> new RuntimeException("Payment not found"));

		if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {

			log.warn("Payment already processed. orderId={}", payment.getOrderId());

			return;
		}

		try {

			/*
			 * TODO: Verify Razorpay Signature
			 */

			payment.setRazorpayPaymentId(request.getRazorpayPaymentId());

			payment.setPaymentStatus(PaymentStatus.SUCCESS);

			paymentRepository.save(payment);

			paymentSuccessProducer.publish(

					PaymentSuccessEvent.builder().orderId(payment.getOrderId())
							.razorpayPaymentId(payment.getRazorpayPaymentId()).build());

			log.info("Payment verified successfully for orderId={}", payment.getOrderId());

		} catch (Exception ex) {

			payment.setPaymentStatus(PaymentStatus.FAILED);

			paymentRepository.save(payment);

			paymentFailedProducer.publish(

					PaymentFailedEvent.builder().orderId(payment.getOrderId()).reason(ex.getMessage()).build());

			throw new RuntimeException("Payment verification failed");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public RazorpayOrderResponse getPaymentByOrderId(Long orderId) {

		Payment payment = paymentRepository.findByOrderId(orderId)
				.orElseThrow(() -> new RuntimeException("Payment not found"));

		return RazorpayOrderResponse.builder().razorpayOrderId(payment.getRazorpayOrderId()).amount(payment.getAmount())
				.currency("INR").key(razorpayConfig.getKeyId()).build();
	}
}