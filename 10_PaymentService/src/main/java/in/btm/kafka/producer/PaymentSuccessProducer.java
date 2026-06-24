package in.btm.kafka.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import in.btm.kafka.event.PaymentSuccessEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentSuccessProducer {

	private final KafkaTemplate<String, Object> kafkaTemplate;

	public void publish(PaymentSuccessEvent event) {

		kafkaTemplate.send("payment-success", event.getOrderId().toString(), event);

		log.info("PaymentSuccessEvent Published : {}", event);
	}
}