package in.btm.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import in.btm.entity.Cart;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    RedisTemplate<String, Cart> redisTemplate(RedisConnectionFactory connectionFactory) {

		RedisTemplate<String, Cart> template = new RedisTemplate<>();

		template.setConnectionFactory(connectionFactory);

		ObjectMapper objectMapper = new ObjectMapper();

		// Support LocalDateTime
		objectMapper.registerModule(new JavaTimeModule());

		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		Jackson2JsonRedisSerializer<Cart> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, Cart.class);

		// Key serializer
		template.setKeySerializer(new StringRedisSerializer());

		template.setHashKeySerializer(new StringRedisSerializer());

		// Value serializer
		template.setValueSerializer(serializer);

		template.setHashValueSerializer(serializer);

		template.afterPropertiesSet();

		return template;
	}
}