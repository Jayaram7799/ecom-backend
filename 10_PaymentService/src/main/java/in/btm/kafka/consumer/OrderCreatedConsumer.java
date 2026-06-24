package in.btm.kafka.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import in.btm.dto.CreatePaymentRequest;
import in.btm.kafka.event.OrderCreatedEvent;
import in.btm.service.PaymentService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderCreatedConsumer {

	private final PaymentService paymentService;

	@KafkaListener(topics = "order-created", groupId = "payment-group")
	public void consume(OrderCreatedEvent event) {
		

		CreatePaymentRequest request = new CreatePaymentRequest();

		request.setOrderId(event.getOrderId());

		request.setCustomerId(event.getCustomerId());

		request.setAmount(event.getTotalAmount());

		paymentService.createPayment(request);
	}
}