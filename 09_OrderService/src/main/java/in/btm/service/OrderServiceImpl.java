package in.btm.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.btm.dto.CreateOrderRequest;
import in.btm.dto.OrderDetailsResponse;
import in.btm.dto.OrderItemRequest;
import in.btm.dto.OrderItemResponse;
import in.btm.dto.OrderResponse;
import in.btm.entity.Order;
import in.btm.entity.OrderItem;
import in.btm.enums.OrderStatus;
import in.btm.enums.PaymentStatus;
import in.btm.kafka.event.OrderCreatedEvent;
import in.btm.kafka.producer.OrderProducer;
import in.btm.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderServiceImpl implements OrderService {

	private final OrderRepository orderRepository;

	private final OrderProducer orderProducer;

	@Override
	public OrderResponse createOrder(CreateOrderRequest request, Integer customerId) {

		log.info("Creating order for customerId={}", customerId);

		validateOrderRequest(request);

		BigDecimal totalAmount = calculateTotalAmount(request.getItems());

		Integer totalQuantity = calculateTotalQuantity(request.getItems());

		Order order = buildOrder(request, customerId, totalAmount, totalQuantity);

		Order savedOrder = orderRepository.save(order);

		log.info("Order created successfully. orderId={}", savedOrder.getOrderId());

		publishOrderCreatedEvent(savedOrder, customerId);

		return OrderResponse.builder().orderId(savedOrder.getOrderId()).status(savedOrder.getOrderStatus().name())
				.message("Order Created Successfully").build();
	}

	private void validateOrderRequest(CreateOrderRequest request) {

		if (request == null || request.getItems() == null || request.getItems().isEmpty()) {

			throw new RuntimeException("Order items cannot be empty");
		}
	}

	private BigDecimal calculateTotalAmount(List<OrderItemRequest> items) {

		return items.stream()
				.map(item -> BigDecimal.valueOf(item.getPrice()).multiply(BigDecimal.valueOf(item.getQuantity())))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private Integer calculateTotalQuantity(List<OrderItemRequest> items) {

		return items.stream().mapToInt(OrderItemRequest::getQuantity).sum();
	}

	private Order buildOrder(CreateOrderRequest request, Integer customerId, BigDecimal totalAmount,
			Integer totalQuantity) {

		Order order = new Order();

		order.setOrderNumber(generateOrderNumber());

		order.setUserId(customerId);

		order.setShippingAddressId(request.getAddressId().longValue());

		order.setTotalAmount(totalAmount);

		order.setTotalQuantity(totalQuantity);

		order.setOrderStatus(OrderStatus.PENDING_PAYMENT);

		order.setPaymentStatus(PaymentStatus.PENDING);

		for (OrderItemRequest item : request.getItems()) {

			OrderItem orderItem = new OrderItem();

			orderItem.setProductId(item.getProductId().longValue());

			orderItem.setProductName(item.getProductName());

			orderItem.setQuantity(item.getQuantity());

			orderItem.setUnitPrice(BigDecimal.valueOf(item.getPrice()));

			orderItem.setTotalPrice(
					BigDecimal.valueOf(item.getPrice()).multiply(BigDecimal.valueOf(item.getQuantity())));

			orderItem.setOrder(order);

			order.getOrderItems().add(orderItem);
		}

		return order;
	}

	private String generateOrderNumber() {

		return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
	}

	private void publishOrderCreatedEvent(Order order, Integer customerId) {

		OrderCreatedEvent event = OrderCreatedEvent.builder().orderId(order.getOrderId()).customerId(customerId)
				.totalAmount(order.getTotalAmount()).build();

		orderProducer.publishOrderCreated(event);

		log.info("OrderCreatedEvent published. orderId={}", order.getOrderId());
	}

	@Override
	@Transactional(readOnly = true)
	public Order getOrderById(Long orderId) {

		return orderRepository.findById(orderId)
				.orElseThrow(() -> new RuntimeException("Order not found : " + orderId));
	}

	@Override
	@Transactional(readOnly = true)
	public List<Order> getOrdersByCustomerId(Integer customerId) {

		return orderRepository.findByUserId(customerId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Order> getAllOrders() {

		return orderRepository.findAll();
	}

	@Override
	public void cancelOrder(Long orderId) {

		Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));

		if (order.getOrderStatus() == OrderStatus.DELIVERED) {

			throw new RuntimeException("Delivered orders cannot be cancelled");
		}

		if (order.getOrderStatus() == OrderStatus.CANCELLED) {

			throw new RuntimeException("Order already cancelled");
		}

		order.setOrderStatus(OrderStatus.CANCELLED);

		orderRepository.save(order);

		log.info("Order cancelled. orderId={}", orderId);
	}

	@Override
	@Transactional(readOnly = true)
	public OrderDetailsResponse getOrderDetails(Long orderId) {

		Order order = orderRepository.findOrderWithItems(orderId)
				.orElseThrow(() -> new RuntimeException("Order not found"));

		return mapToOrderDetailsResponse(order);
	}

	private OrderDetailsResponse mapToOrderDetailsResponse(Order order) {

		return OrderDetailsResponse.builder()

				.orderId(order.getOrderId())

				.orderNumber(order.getOrderNumber())

				.totalAmount(order.getTotalAmount())

				.totalQuantity(order.getTotalQuantity())

				.orderStatus(order.getOrderStatus().name())

				.paymentStatus(order.getPaymentStatus().name())

				.razorpayOrderId(order.getRazorpayOrderId())

				.razorpayPaymentId(order.getRazorpayPaymentId())

				.createdAt(order.getCreatedAt())

				.items(order.getOrderItems().stream().map(this::mapToOrderItemResponse).toList())

				.build();
	}

	private OrderItemResponse mapToOrderItemResponse(OrderItem item) {

		return OrderItemResponse.builder()

				.productId(item.getProductId())

				.productName(item.getProductName())

				.quantity(item.getQuantity())

				.unitPrice(item.getUnitPrice())

				.totalPrice(item.getTotalPrice())

				.build();
	}
}