package in.btm.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;

import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
@EnableKafka
public class KafkaConfig {

	/*
	 * ========================= PRODUCER CONFIG =========================
	 */

	@Bean
	public ProducerFactory<String, Object> producerFactory() {

		Map<String, Object> props = new HashMap<>();

		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

		return new DefaultKafkaProducerFactory<>(props);
	}

	@Bean
	public KafkaTemplate<String, Object> kafkaTemplate() {

		return new KafkaTemplate<>(producerFactory());
	}

	/*
	 * ========================= CONSUMER CONFIG =========================
	 */

	@Bean
	public ConsumerFactory<String, Object> consumerFactory() {

		JsonDeserializer<Object> deserializer = new JsonDeserializer<>();

		deserializer.addTrustedPackages("*");

		Map<String, Object> props = new HashMap<>();

		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

		props.put(ConsumerConfig.GROUP_ID_CONFIG, "order-group-v2");

		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

		return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {

		ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();

		factory.setConsumerFactory(consumerFactory());

		return factory;
	}

	/*
	 * ========================= TOPICS =========================
	 */

	@Bean
	public NewTopic orderCreatedTopic() {

		return TopicBuilder.name("order-created").partitions(3).replicas(1).build();
	}

	@Bean
	public NewTopic paymentInitiatedTopic() {

		return TopicBuilder.name("payment-initiated").partitions(3).replicas(1).build();
	}

	@Bean
	public NewTopic paymentSuccessTopic() {

		return TopicBuilder.name("payment-success").partitions(3).replicas(1).build();
	}

	@Bean
	public NewTopic paymentFailedTopic() {

		return TopicBuilder.name("payment-failed").partitions(3).replicas(1).build();
	}
}