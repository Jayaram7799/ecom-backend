package in.btm.kafka.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import in.btm.entity.Order;
import in.btm.repository.OrderRepository;
import in.btm.kafka.event.PaymentInitiatedEvent;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentInitiatedConsumer {

	private static final Logger log = LoggerFactory.getLogger(PaymentInitiatedConsumer.class);

	private final OrderRepository orderRepository;

	@KafkaListener(topics = "payment-initiated", groupId = "order-group-v2")
	public void consume(PaymentInitiatedEvent event) {

		log.info("Received PaymentInitiatedEvent : {}", event);

		Order order = orderRepository.findById(event.getOrderId())
				.orElseThrow(() -> new RuntimeException("Order not found"));

		order.setRazorpayOrderId(event.getRazorpayOrderId());

		orderRepository.save(order);

		log.info("Order updated with RazorpayOrderId : {}", event.getRazorpayOrderId());
	}
}