package in.btm.kafka.event;

import org.springframework.stereotype.Component;

import in.btm.entity.Order;
import in.btm.kafka.producer.OrderProducer;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor

public class OrderEventPublisher {

	private final OrderProducer producer;

	public void publish(Order order) {

		producer.publishOrderCreated(OrderCreatedEvent.builder().orderId(order.getOrderId())
				.email(order.getEmail()).totalAmount(order.getTotalAmount()).build());
	}
}