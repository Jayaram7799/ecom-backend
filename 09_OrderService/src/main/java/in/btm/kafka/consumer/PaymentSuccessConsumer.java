package in.btm.kafka.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import in.btm.entity.Order;
import in.btm.enums.OrderStatus;
import in.btm.enums.PaymentStatus;
import in.btm.kafka.event.PaymentSuccessEvent;
import in.btm.repository.OrderRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentSuccessConsumer {

	private static final Logger log = LoggerFactory.getLogger(PaymentSuccessConsumer.class);

	private final OrderRepository orderRepository;

	@KafkaListener(topics = "payment-success", groupId = "order-group-v2")
	public void consume(PaymentSuccessEvent event) {

		log.info("Received PaymentSuccessEvent : {}", event);

		Order order = orderRepository.findById(event.getOrderId())
				.orElseThrow(() -> new RuntimeException("Order not found"));

		order.setOrderStatus(OrderStatus.CONFIRMED);

		order.setPaymentStatus(PaymentStatus.SUCCESS);

		order.setRazorpayPaymentId(event.getRazorpayPaymentId());

		orderRepository.save(order);

		log.info("Order {} confirmed successfully", order.getOrderId());
	}
}