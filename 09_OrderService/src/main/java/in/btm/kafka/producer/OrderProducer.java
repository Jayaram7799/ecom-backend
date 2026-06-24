package in.btm.kafka.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import in.btm.kafka.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderProducer {

	private final KafkaTemplate<String, Object> kafkaTemplate;

	public void publishOrderCreated(OrderCreatedEvent event) {

		kafkaTemplate.send("order-created", event.getOrderId().toString(), event);

		log.info("OrderCreatedEvent Published : {}", event);
	}
}