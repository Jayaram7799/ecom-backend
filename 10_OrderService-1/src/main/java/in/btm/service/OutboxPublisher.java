package in.btm.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import in.btm.entity.OutboxEvent;
import in.btm.entity.OutboxStatus;
import in.btm.kafka.event.OrderCreatedEvent;
import in.btm.repository.OutboxEventRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

	private final OutboxEventRepository outboxEventRepository;

	private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

	private final ObjectMapper objectMapper;

	@Value("${outbox.publisher.batch-size:100}")
	private int batchSize;

	@Scheduled(fixedDelayString = "${outbox.publisher.fixed-delay:5000}")
	@Transactional
	public void publishEvents() {

		List<OutboxEvent> events = outboxEventRepository.findPendingEvents(OutboxStatus.PENDING);

		if (events.isEmpty()) {
			return;
		}

		log.info("Found {} pending outbox events", events.size());

		/*
		 * Process only configured batch size.
		 */
		events.stream().limit(batchSize).forEach(this::processEvent);
	}

	private void processEvent(OutboxEvent event) {

		try {

			log.info("Publishing outbox event. id={}, eventId={}, type={}", event.getId(), event.getEventId(),
					event.getEventType());

			publishEvent(event);

			markAsProcessed(event);

			log.info("Outbox event processed successfully. id={}, eventId={}", event.getId(), event.getEventId());

		} catch (JsonProcessingException ex) {

			/*
			 * Payload is invalid.
			 *
			 * Retrying will not fix malformed JSON, so this is normally a permanent
			 * failure.
			 */
			handlePermanentFailure(event, "Invalid event payload", ex);

		} catch (IllegalArgumentException ex) {

			/*
			 * Unsupported event type or invalid event data.
			 */
			handlePermanentFailure(event, "Invalid outbox event", ex);

		} catch (InterruptedException ex) {

			/*
			 * Restore interrupt status.
			 */
			Thread.currentThread().interrupt();

			handleRetryableFailure(event, "Kafka publish interrupted", ex);

		} catch (ExecutionException ex) {

			/*
			 * Kafka producer failed asynchronously.
			 */
			handleRetryableFailure(event, "Kafka publish failed", ex);

		} catch (TimeoutException ex) {

			/*
			 * Kafka did not respond within timeout.
			 */
			handleRetryableFailure(event, "Kafka publish timed out", ex);

		} catch (RuntimeException ex) {

			/*
			 * Unexpected application/database error.
			 */
			handleRetryableFailure(event, "Unexpected runtime error", ex);
		}
	}

	private void publishEvent(OutboxEvent event)
			throws JsonProcessingException, InterruptedException, ExecutionException, TimeoutException {

		if ("OrderCreatedEvent".equals(event.getEventType())) {

			OrderCreatedEvent orderCreatedEvent = objectMapper.readValue(event.getPayload(), OrderCreatedEvent.class);

			kafkaTemplate.send("order-created", event.getAggregateId(), orderCreatedEvent).get(10, TimeUnit.SECONDS);

			return;
		}

		throw new IllegalArgumentException("Unsupported event type: " + event.getEventType());
	}

	private void markAsProcessed(OutboxEvent event) {

		LocalDateTime now = LocalDateTime.now();

		event.setStatus(OutboxStatus.PROCESSED);

		event.setProcessedAt(now);

		event.setLastAttemptAt(now);

		event.setErrorMessage(null);

		outboxEventRepository.save(event);
	}

	private void handleRetryableFailure(OutboxEvent event, String message, Exception ex) {

		int retryCount = event.getRetryCount() + 1;

		event.setRetryCount(retryCount);

		event.setLastAttemptAt(LocalDateTime.now());

		event.setErrorMessage(getErrorMessage(ex));

		if (retryCount >= 10) {

			event.setStatus(OutboxStatus.FAILED);

			log.error("Outbox event permanently failed. " + "id={}, eventId={}, retryCount={}, reason={}",
					event.getId(), event.getEventId(), retryCount, message, ex);

		} else {

			event.setStatus(OutboxStatus.PENDING);

			log.warn("Outbox event publish failed. " + "id={}, eventId={}, retryCount={}, reason={}", event.getId(),
					event.getEventId(), retryCount, message, ex);
		}

		outboxEventRepository.save(event);
	}

	private void handlePermanentFailure(OutboxEvent event, String message, Exception ex) {

		event.setStatus(OutboxStatus.FAILED);

		event.setLastAttemptAt(LocalDateTime.now());

		event.setErrorMessage(getErrorMessage(ex));

		log.error("Outbox event permanently failed. " + "id={}, eventId={}, reason={}", event.getId(),
				event.getEventId(), message, ex);

		outboxEventRepository.save(event);
	}

	private String getErrorMessage(Exception ex) {

		String message = ex.getMessage();

		if (message == null || message.isBlank()) {

			return ex.getClass().getSimpleName();
		}

		return message.length() > 1000 ? message.substring(0, 1000) : message;
	}
}