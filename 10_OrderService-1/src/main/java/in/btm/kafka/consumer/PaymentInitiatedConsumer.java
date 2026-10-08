package in.btm.kafka.consumer;

import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import in.btm.entity.Order;
import in.btm.enums.PaymentStatus;
import in.btm.exception.OrderNotFoundException;
import in.btm.kafka.event.PaymentInitiatedEvent;
import in.btm.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentInitiatedConsumer {

	private final OrderRepository orderRepository;

	@RetryableTopic(attempts = "4",

			backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 10000),

			dltTopicSuffix = "-dlt",

			topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_DELAY_VALUE,

			exclude = { OrderNotFoundException.class })
	@KafkaListener(topics = "payment-initiated", groupId = "order-group-v2")
	@Transactional
	public void consume(PaymentInitiatedEvent event) {

		log.info("Received PaymentInitiatedEvent. " + "eventId={}, orderId={}, razorpayOrderId={}", event.getEventId(),
				event.getOrderId(), event.getRazorpayOrderId());

		Order order = orderRepository.findById(event.getOrderId())
				.orElseThrow(() -> new OrderNotFoundException("Order Not Found "+event.getOrderId()));

		/*
		 * Idempotent state update.
		 *
		 * If the same event is delivered again, setting the same values is harmless.
		 */
		order.setRazorpayOrderId(event.getRazorpayOrderId());

		order.setPaymentStatus(PaymentStatus.PENDING);

		orderRepository.save(order);

		log.info("Order updated with Razorpay order ID. " + "orderId={}, razorpayOrderId={}", order.getOrderId(),
				event.getRazorpayOrderId());
	}

	@DltHandler
	public void handleDlt(PaymentInitiatedEvent event) {

		log.error("PaymentInitiatedEvent moved to DLT. " + "eventId={}, orderId={}, razorpayOrderId={}",
				event.getEventId(), event.getOrderId(), event.getRazorpayOrderId());

		/*
		 * Production:
		 *
		 * 1. Store DLT event 2. Store exception information 3. Alert operations 4.
		 * Create monitoring metric 5. Support manual replay
		 */
	}
}