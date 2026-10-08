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
import in.btm.dto.OrderSummaryResponse;
import in.btm.dto.ProductInternalResponse;
import in.btm.entity.Order;
import in.btm.entity.OrderItem;
import in.btm.enums.OrderStatus;
import in.btm.enums.PaymentStatus;
import in.btm.exception.InvalidOrderException;
import in.btm.exception.OrderNotFoundException;
import in.btm.feign.ProductClient;
import in.btm.kafka.event.OrderCreatedEvent;
import in.btm.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

	private final OrderRepository orderRepository;

	private final OutboxService outboxService;

	private final ProductClient productClient;

	// ============================================================
	// CREATE ORDER
	// ============================================================

	@Override
	@Transactional
	public OrderResponse createOrder(CreateOrderRequest request, String email) {

		log.info("Creating order. customerEmail={}", email);

		// --------------------------------------------------------
		// 1. Validate request
		// --------------------------------------------------------

		validateCreateOrderRequest(request);

		// --------------------------------------------------------
		// 2. Create Order
		// --------------------------------------------------------

		Order order = new Order();

		order.setOrderNumber(generateOrderNumber());

		/*
		 * Email comes directly from Spring Security Authentication object.
		 *
		 * No UserProfile lookup required.
		 */
		order.setCustomerEmail(email);

		order.setShippingAddressId(request.getAddressId().intValue());

		/*
		 * Initial order state.
		 */
		order.setOrderStatus(OrderStatus.PENDING_PAYMENT);

		/*
		 * Initial payment state.
		 */
		order.setPaymentStatus(PaymentStatus.NOT_INITIATED);

		// --------------------------------------------------------
		// 3. Fetch products and create order items
		// --------------------------------------------------------

		BigDecimal totalAmount = BigDecimal.ZERO;

		int totalQuantity = 0;

		for (OrderItemRequest itemRequest : request.getItems()) {

			// ----------------------------------------------------
			// 3.1 Fetch product from ProductService
			// ----------------------------------------------------

			ProductInternalResponse product = productClient.getProduct(itemRequest.getProductId());
			
			System.out.println("Product Image "+product.getImageUrl());

			// ----------------------------------------------------
			// 3.2 Validate product
			// ----------------------------------------------------

			validateProduct(product, itemRequest);

			// ----------------------------------------------------
			// 3.3 Calculate item total
			// ----------------------------------------------------

			BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

			// ----------------------------------------------------
			// 3.4 Create OrderItem
			// ----------------------------------------------------

			OrderItem orderItem = buildOrderItem(itemRequest, product, order);

			order.getOrderItems().add(orderItem);

			// ----------------------------------------------------
			// 3.5 Calculate order totals
			// ----------------------------------------------------

			totalAmount = totalAmount.add(itemTotal);

			totalQuantity += itemRequest.getQuantity();
		}

		// --------------------------------------------------------
		// 4. Set calculated totals
		// --------------------------------------------------------

		order.setTotalAmount(totalAmount);

		order.setTotalQuantity(totalQuantity);

		// --------------------------------------------------------
		// 5. Save Order
		// --------------------------------------------------------

		Order savedOrder = orderRepository.save(order);

		log.info(
				"Order saved successfully. " + "orderId={}, orderNumber={}, customerEmail={}, "
						+ "totalAmount={}, totalQuantity={}",
				savedOrder.getOrderId(), savedOrder.getOrderNumber(), savedOrder.getCustomerEmail(),
				savedOrder.getTotalAmount(), savedOrder.getTotalQuantity());

		// --------------------------------------------------------
		// 6. Create OrderCreatedEvent
		// --------------------------------------------------------

		UUID eventId = UUID.randomUUID();

		OrderCreatedEvent event = OrderCreatedEvent.builder().eventId(eventId).orderId(savedOrder.getOrderId())
				.orderNumber(savedOrder.getOrderNumber()).customerEmail(savedOrder.getCustomerEmail())
				.totalAmount(savedOrder.getTotalAmount()).totalQuantity(savedOrder.getTotalQuantity()).build();

		// --------------------------------------------------------
		// 7. Save Event into Outbox
		// --------------------------------------------------------

		/*
		 * IMPORTANT:
		 *
		 * We do NOT call Kafka directly.
		 *
		 * Order + OrderItems + OutboxEvent
		 *
		 * are committed in the SAME database transaction.
		 *
		 * OutboxPublisher will later publish the event to Kafka.
		 */
		outboxService.saveOrderCreatedEvent(event);

		log.info("OrderCreatedEvent saved to outbox. " + "eventId={}, orderId={}", eventId, savedOrder.getOrderId());

		// --------------------------------------------------------
		// 8. Build Response
		// --------------------------------------------------------

		return OrderResponse.builder().orderId(savedOrder.getOrderId()).orderNumber(savedOrder.getOrderNumber())
				.status(savedOrder.getOrderStatus().name()).message("Order created successfully").build();
	}

	// ============================================================
	// GET ORDER BY ID
	// ============================================================

	@Override
	public Order getOrderById(Long orderId) {

		log.debug("Fetching order. orderId={}", orderId);

		return orderRepository.findById(orderId)
				.orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
	}

	// ============================================================
	// GET ORDER DETAILS
	// ============================================================

	@Override
	public OrderDetailsResponse getOrderDetails(Long orderId, String email) {

		log.debug("Fetching order details. " + "orderId={}, customerEmail={}", orderId, email);

		// --------------------------------------------------------
		// 1. Find order
		// --------------------------------------------------------

		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

		// --------------------------------------------------------
		// 2. Verify ownership
		// --------------------------------------------------------

		if (!order.getCustomerEmail().equalsIgnoreCase(email)) {

			log.warn("Unauthorized order access attempt. " + "orderId={}, customerEmail={}", orderId, email);

			throw new OrderNotFoundException("Order not found with id: " + orderId);
		}

		// --------------------------------------------------------
		// 3. Map entity to DTO
		// --------------------------------------------------------

		return mapToOrderDetailsResponse(order);
	}

	// ============================================================
	// GET CUSTOMER ORDERS
	// ============================================================

	@Override
	@Transactional(readOnly = true)
	public List<OrderSummaryResponse> getOrdersByEmail(String email) {

	    log.debug(
	            "Fetching customer orders. customerEmail={}",
	            email
	    );

	    List<Order> orders =
	            orderRepository
	                    .findByCustomerEmailIgnoreCaseOrderByCreatedAtDesc(
	                            email
	                    );

	    return orders.stream()
	            .map(this::mapToOrderSummaryResponse)
	            .toList();
	}
	// ============================================================
	// GET ALL ORDERS
	// ============================================================

	@Override
	public List<Order> getAllOrders() {

		log.debug("Fetching all orders");

		return orderRepository.findAll();
	}

	// ============================================================
	// CANCEL ORDER
	// ============================================================

	@Override
	@Transactional
	public void cancelOrder(Long orderId, String email) {

		log.info("Cancelling order. " + "orderId={}, customerEmail={}", orderId, email);

		// --------------------------------------------------------
		// 1. Find order
		// --------------------------------------------------------

		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

		// --------------------------------------------------------
		// 2. Verify ownership
		// --------------------------------------------------------

		if (!order.getCustomerEmail().equalsIgnoreCase(email)) {

			log.warn("Unauthorized order cancellation attempt. " + "orderId={}, customerEmail={}", orderId, email);

			throw new OrderNotFoundException("Order not found with id: " + orderId);
		}

		// --------------------------------------------------------
		// 3. Validate current status
		// --------------------------------------------------------

		validateCancellation(order.getOrderStatus());

		// --------------------------------------------------------
		// 4. Cancel order
		// --------------------------------------------------------

		order.setOrderStatus(OrderStatus.CANCELLED);

		log.info("Order cancelled successfully. " + "orderId={}, customerEmail={}", orderId, email);

		/*
		 * No explicit save() required.
		 *
		 * Order is a managed entity inside the transaction. Hibernate dirty checking
		 * will persist the change.
		 */
	}

	// ============================================================
	// VALIDATE CREATE ORDER REQUEST
	// ============================================================

	private void validateCreateOrderRequest(CreateOrderRequest request) {

		if (request == null) {

			throw new InvalidOrderException("Order request cannot be null");
		}

		if (request.getAddressId() == null) {

			throw new InvalidOrderException("Shipping address is required");
		}

		if (request.getItems() == null || request.getItems().isEmpty()) {

			throw new InvalidOrderException("Order must contain at least one item");
		}
	}

	// ============================================================
	// BUILD ORDER ITEM
	// ============================================================

	private OrderItem buildOrderItem(OrderItemRequest itemRequest, ProductInternalResponse product, Order order) {

		BigDecimal totalPrice = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

		OrderItem orderItem = new OrderItem();

		/*
		 * Product information comes from ProductService.
		 */
		orderItem.setProductId(product.getId().longValue());

		orderItem.setProductName(product.getName());

		/*
		 * Quantity comes from customer request.
		 */
		orderItem.setQuantity(itemRequest.getQuantity());

		/*
		 * Price comes from ProductService. Never trust frontend price.
		 */
		orderItem.setUnitPrice(product.getPrice());

		orderItem.setTotalPrice(totalPrice);

		/*
		 * Maintain bidirectional relationship.
		 */
		orderItem.setOrder(order);
		orderItem.setProductImageUrl(product.getImageUrl());

		return orderItem;
	}

	// ============================================================
	// VALIDATE PRODUCT
	// ============================================================

	private void validateProduct(ProductInternalResponse product, OrderItemRequest itemRequest) {

		// --------------------------------------------------------
		// Product exists
		// --------------------------------------------------------

		if (product == null) {

			throw new InvalidOrderException("Product not found. productId=" + itemRequest.getProductId());
		}

		// --------------------------------------------------------
		// Product ID validation
		// --------------------------------------------------------

		if (product.getId() == null) {

			throw new InvalidOrderException("Invalid product response. productId=" + itemRequest.getProductId());
		}

		// --------------------------------------------------------
		// Product active
		// --------------------------------------------------------

		/*
		 * if (!Boolean.TRUE.equals(product.getActive())) {
		 * 
		 * throw new InvalidOrderException("Product is inactive. productId=" +
		 * itemRequest.getProductId()); }
		 */

		// --------------------------------------------------------
		// Product price
		// --------------------------------------------------------

		if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {

			throw new InvalidOrderException("Invalid product price. productId=" + itemRequest.getProductId());
		}

		// --------------------------------------------------------
		// Quantity
		// --------------------------------------------------------

		if (itemRequest.getQuantity() == null || itemRequest.getQuantity() <= 0) {

			throw new InvalidOrderException(
					"Quantity must be greater than zero. " + "productId=" + itemRequest.getProductId());
		}

		// --------------------------------------------------------
		// Available stock
		// --------------------------------------------------------

		if (product.getAvailableQuantity() == null || product.getAvailableQuantity() <= 0) {

			throw new InvalidOrderException("Product is out of stock. productId=" + itemRequest.getProductId());
		}

		// --------------------------------------------------------
		// Requested quantity <= available quantity
		// --------------------------------------------------------

		if (itemRequest.getQuantity() > product.getAvailableQuantity()) {

			throw new InvalidOrderException("Insufficient stock for productId=" + itemRequest.getProductId()
					+ ". Available=" + product.getAvailableQuantity());
		}
	}

	// ============================================================
	// VALIDATE ORDER CANCELLATION
	// ============================================================

	private void validateCancellation(OrderStatus status) {

		if (status == null) {

			throw new InvalidOrderException("Order status cannot be null");
		}

		// --------------------------------------------------------
		// Already cancelled
		// --------------------------------------------------------

		if (status == OrderStatus.CANCELLED) {

			throw new InvalidOrderException("Order is already cancelled");
		}

		// --------------------------------------------------------
		// Delivered
		// --------------------------------------------------------

		if (status == OrderStatus.DELIVERED) {

			throw new InvalidOrderException("Delivered order cannot be cancelled");
		}

		// --------------------------------------------------------
		// Shipped / Out for delivery
		// --------------------------------------------------------

		if (status == OrderStatus.SHIPPED || status == OrderStatus.OUT_FOR_DELIVERY) {

			throw new InvalidOrderException("Order cannot be cancelled after shipping");
		}
	}

	// ============================================================
	// MAP ORDER DETAILS
	// ============================================================

	private OrderDetailsResponse mapToOrderDetailsResponse(Order order) {

		return OrderDetailsResponse.builder().orderId(order.getOrderId()).orderNumber(order.getOrderNumber())
				.customerEmail(order.getCustomerEmail()).shippingAddressId(order.getShippingAddressId())
				.totalAmount(order.getTotalAmount()).totalQuantity(order.getTotalQuantity())
				.orderStatus(order.getOrderStatus())

				// Payment details
				.paymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus().name() : null)
				.razorpayOrderId(order.getRazorpayOrderId()).razorpayPaymentId(order.getRazorpayPaymentId())

				.createdAt(order.getCreatedAt()).updatedAt(order.getUpdatedAt()).build();
	}
	
	private OrderSummaryResponse mapToOrderSummaryResponse(Order order) {

	    List<OrderItemResponse> items =
	            order.getOrderItems()
	                    .stream()
	                    .map(this::mapToOrderItemResponse)
	                    .toList();

	    return OrderSummaryResponse.builder()
	            .orderId(order.getOrderId())
	            .orderNumber(order.getOrderNumber())
	            .totalAmount(order.getTotalAmount())
	            .totalQuantity(order.getTotalQuantity())
	            .customerEmail(order.getCustomerEmail())
	            .orderStatus(order.getOrderStatus())
	            .shippingAddressId(order.getShippingAddressId())
	            .paymentStatus(
	                    order.getPaymentStatus() != null
	                            ? order.getPaymentStatus().name()
	                            : null
	            )
	            .razorpayOrderId(order.getRazorpayOrderId())
	            .razorpayPaymentId(order.getRazorpayPaymentId())
	            .createdAt(order.getCreatedAt())
	            .updatedAt(order.getUpdatedAt())
	            .items(items)
	            .build();
	}
	
	private OrderItemResponse mapToOrderItemResponse(OrderItem item) {

	    return OrderItemResponse.builder()
	            .id(item.getOrderItemId())
	            .productId(item.getProductId())
	            .productName(item.getProductName())
	            .quantity(item.getQuantity())
	            .price(item.getTotalPrice())
	            .subtotal(
	                    item.getTotalPrice()
	                            .multiply(
	                                    BigDecimal.valueOf(
	                                            item.getQuantity()
	                                    )
	                            )
	            )
	            .imageUrl(item.getProductImageUrl())
	            .build();
	}

	// ============================================================
	// GENERATE ORDER NUMBER
	// ============================================================

	private String generateOrderNumber() {

		return "ORD-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
	}
}