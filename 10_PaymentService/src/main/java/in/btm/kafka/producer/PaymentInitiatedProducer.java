package in.btm.kafka.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import in.btm.kafka.event.PaymentInitiatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentInitiatedProducer {

	private final KafkaTemplate<String, Object> kafkaTemplate;

	public void publish(PaymentInitiatedEvent event) {

		kafkaTemplate.send("payment-initiated", event.getOrderId().toString(), event);

		log.info("PaymentInitiatedEvent Published : {}", event);
	}
}