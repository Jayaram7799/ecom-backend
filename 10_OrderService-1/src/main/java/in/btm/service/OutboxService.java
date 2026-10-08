package in.btm.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import in.btm.entity.OutboxEvent;
import in.btm.entity.OutboxStatus;
import in.btm.kafka.event.OrderCreatedEvent;
import in.btm.repository.OutboxEventRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OutboxService {

	private final OutboxEventRepository outboxEventRepository;

	private final ObjectMapper objectMapper;

	public void saveOrderCreatedEvent(OrderCreatedEvent event) {

		try {

			String payload = objectMapper.writeValueAsString(event);

			OutboxEvent outboxEvent = OutboxEvent.builder().eventId(event.getEventId()).aggregateType("Order")
					.aggregateId(event.getOrderId().toString()).eventType("OrderCreatedEvent").payload(payload)
					.status(OutboxStatus.PENDING).retryCount(0).createdAt(LocalDateTime.now()).build();

			outboxEventRepository.save(outboxEvent);

		} catch (JsonProcessingException e) {

			throw new IllegalStateException("Failed to serialize OrderCreatedEvent", e);
		}
	}
}