package in.btm.kafka.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import in.btm.kafka.event.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentFailedProducer {

	private final KafkaTemplate<String, Object> kafkaTemplate;

	public void publish(PaymentFailedEvent event) {

		kafkaTemplate.send("payment-failed", event.getOrderId().toString(), event);

		log.info("PaymentFailedEvent Published : {}", event);
	}
}