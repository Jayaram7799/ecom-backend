package in.btm.kafka.consumer;

import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import in.btm.entity.Order;
import in.btm.enums.OrderStatus;
import in.btm.enums.PaymentStatus;
import in.btm.exception.OrderNotFoundException;
import in.btm.kafka.event.PaymentFailedEvent;
import in.btm.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentFailedConsumer {

	private final OrderRepository orderRepository;

	@RetryableTopic(attempts = "4",

			backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 10000),

			dltTopicSuffix = "-dlt",

			topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_DELAY_VALUE,

			exclude = { OrderNotFoundException.class })
	@KafkaListener(topics = "payment-failed", groupId = "order-group-v2")
	@Transactional
	public void consume(PaymentFailedEvent event) {

		log.info("Received PaymentFailedEvent. " + "eventId={}, orderId={}", event.getEventId(), event.getOrderId());

		Order order = orderRepository.findById(event.getOrderId())
				.orElseThrow(() -> new OrderNotFoundException("Order Not Found " + event.getOrderId()));

		order.setOrderStatus(OrderStatus.PAYMENT_FAILED);

		order.setPaymentStatus(PaymentStatus.FAILED);

		orderRepository.save(order);

		log.info("Order marked as PAYMENT_FAILED. " + "orderId={}", order.getOrderId());
	}

	@DltHandler
	public void handleDlt(PaymentFailedEvent event) {

		log.error("PaymentFailedEvent moved to DLT. " + "eventId={}, orderId={}", event.getEventId(),
				event.getOrderId());

		/*
		 * Production:
		 *
		 * Save DLT record Alert operations Metrics Manual replay
		 */
	}
}