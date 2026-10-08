package in.btm.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonSerializer;

import in.btm.kafka.event.OrderCreatedEvent;

@Configuration
public class KafkaConfig {

	@Bean
	public ProducerFactory<String, OrderCreatedEvent> producerFactory() {

		Map<String, Object> config = new HashMap<>();

		config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

		config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

		config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

		config.put(ProducerConfig.ACKS_CONFIG, "all");

		config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

		config.put(ProducerConfig.RETRIES_CONFIG, 10);

		config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);

		return new DefaultKafkaProducerFactory<>(config);
	}

	@Bean
	public KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate() {

		return new KafkaTemplate<>(producerFactory());
	}
}