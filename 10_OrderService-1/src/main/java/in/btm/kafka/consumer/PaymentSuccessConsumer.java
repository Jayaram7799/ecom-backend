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
import in.btm.kafka.event.PaymentSuccessEvent;
import in.btm.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentSuccessConsumer {

	private final OrderRepository orderRepository;

	@RetryableTopic(attempts = "4",

			backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 10000),

			dltTopicSuffix = "-dlt",

			topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_DELAY_VALUE,

			exclude = { OrderNotFoundException.class })
	@KafkaListener(topics = "payment-success", groupId = "order-group-v2")
	@Transactional
	public void consume(PaymentSuccessEvent event) {

		log.info("Received PaymentSuccessEvent. " + "eventId={}, orderId={}, razorpayPaymentId={}", event.getEventId(),
				event.getOrderId(), event.getRazorpayPaymentId());

		Order order = orderRepository.findById(event.getOrderId())
				.orElseThrow(() -> new OrderNotFoundException("Order Not found with Id "+event.getOrderId()));

		/*
		 * Update payment information.
		 */
		order.setPaymentStatus(PaymentStatus.SUCCESS);

		order.setRazorpayPaymentId(event.getRazorpayPaymentId());

		/*
		 * Payment succeeded, therefore order can be confirmed.
		 */
		order.setOrderStatus(OrderStatus.CONFIRMED);

		orderRepository.save(order);

		log.info("Order confirmed successfully. " + "orderId={}, razorpayPaymentId={}", order.getOrderId(),
				event.getRazorpayPaymentId());
	}

	@DltHandler
	public void handleDlt(PaymentSuccessEvent event) {

		log.error("PaymentSuccessEvent moved to DLT. " + "eventId={}, orderId={}, razorpayPaymentId={}",
				event.getEventId(), event.getOrderId(), event.getRazorpayPaymentId());

		/*
		 * Production:
		 *
		 * Save DLT record Alert operations Metrics Manual replay
		 */
	}
}