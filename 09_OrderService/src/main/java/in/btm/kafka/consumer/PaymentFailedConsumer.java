package in.btm.kafka.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import in.btm.entity.Order;
import in.btm.enums.OrderStatus;
import in.btm.enums.PaymentStatus;
import in.btm.kafka.event.PaymentFailedEvent;
import in.btm.repository.OrderRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentFailedConsumer {

	private static final Logger log = LoggerFactory.getLogger(PaymentFailedConsumer.class);

	private final OrderRepository orderRepository;

	@KafkaListener(topics = "payment-failed", groupId = "order-group-v2")
	public void consume(PaymentFailedEvent event) {

		log.info("Received PaymentFailedEvent : {}", event);

		Order order = orderRepository.findById(event.getOrderId())
				.orElseThrow(() -> new RuntimeException("Order not found"));

		order.setOrderStatus(OrderStatus.PAYMENT_FAILED);

		order.setPaymentStatus(PaymentStatus.FAILED);

		orderRepository.save(order);

		log.info("Order {} marked as PAYMENT_FAILED", order.getOrderId());
	}
}