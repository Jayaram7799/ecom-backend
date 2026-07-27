package in.btm.service;

import java.math.BigDecimal;

import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;

import in.btm.dto.CreateOrderRequest;
import in.btm.dto.OrderDetailsResponse;
import in.btm.dto.OrderItemRequest;
import in.btm.dto.OrderResponse;
import in.btm.dto.ProductInternalResponse;
import in.btm.entity.Order;
import in.btm.entity.OrderItem;
import in.btm.enums.OrderStatus;
import in.btm.enums.PaymentStatus;
import in.btm.exception.InvalidOrderException;
import in.btm.exception.OrderNotFoundException;
import in.btm.feign.ProductClient;
import in.btm.kafka.event.OrderEventPublisher;
import in.btm.mapper.OrderMapper;
import in.btm.repository.OrderItemRepository;
import in.btm.repository.OrderRepository;
import in.btm.validator.OrderValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderServiceImpl implements OrderService {

	private final OrderRepository orderRepository;
	private final OrderValidator orderValidator;
	private final OrderMapper orderMapper;
	private final OrderEventPublisher orderEventPublisher;
	private final ProductClient productClient;

	@Override
	public OrderResponse createOrder(CreateOrderRequest request,
	                                 String email) {

	    log.info("Order creation started. customerId={}, addressId={}, totalItems={}",
	    		email,
	            request.getAddressId(),
	            request.getItems().size());

	    orderValidator.validate(request);

	    log.debug("Order request validation completed. customerId={}", email);

	    Order order = new Order();

	    order.setOrderNumber(generateOrderNumber());
	    order.setEmail(email);
	    order.setShippingAddressId(request.getAddressId().longValue());
	    order.setOrderStatus(OrderStatus.PENDING_PAYMENT);
	    order.setPaymentStatus(PaymentStatus.PENDING);

	    BigDecimal totalAmount = BigDecimal.ZERO;
	    int totalQuantity = 0;

	    for (OrderItemRequest dto : request.getItems()) {

	        log.debug("Fetching product details. productId={}, quantity={}",
	                dto.getProductId(),
	                dto.getQuantity());

	        ProductInternalResponse product =
	                productClient.getProduct(dto.getProductId());

	        validateProduct(product, dto);

	        log.debug("Product validation successful. productId={}, productName={}, price={}",
	                product.getId(),
	                product.getName(),
	                product.getPrice());

	        OrderItem item = orderMapper.toOrderItem(dto, product);

	        item.setOrder(order);

	        order.getOrderItems().add(item);

	        totalAmount = totalAmount.add(item.getTotalPrice());

	        totalQuantity += item.getQuantity();
	    }

	    order.setTotalAmount(totalAmount);
	    order.setTotalQuantity(totalQuantity);

	    log.info("Persisting order. customerId={}, orderNumber={}, totalAmount={}, totalQuantity={}",
	            email,
	            order.getOrderNumber(),
	            totalAmount,
	            totalQuantity);

	    Order savedOrder = orderRepository.save(order);

	    log.info("Order persisted successfully. orderId={}, orderNumber={}",
	            savedOrder.getOrderId(),
	            savedOrder.getOrderNumber());

	    orderEventPublisher.publish(savedOrder);

	    log.info("OrderCreatedEvent published successfully. orderId={}, orderNumber={}",
	            savedOrder.getOrderId(),
	            savedOrder.getOrderNumber());

	    log.info("Order creation completed successfully. orderId={}, customerId={}",
	            savedOrder.getOrderId(),
	            email);

	    return OrderResponse.builder()
	            .orderId(savedOrder.getOrderId())
	            .status(savedOrder.getOrderStatus().name())
	            .message("Order Created Successfully")
	            .build();
	}
	@Override
	@Transactional(readOnly = true)
	public OrderDetailsResponse getOrderDetails(Long orderId) {

		log.info("Fetching order details. orderId={}", orderId);

		Order order = orderRepository.findOrderWithItems(orderId)
		        .orElseThrow(() -> new OrderNotFoundException(orderId));

		log.info("Order details fetched successfully. orderId={}, orderNumber={}",
		        order.getOrderId(),
		        order.getOrderNumber());

		return orderMapper.toOrderDetails(order);
	}

	@Override
	public Order getOrderById(Long orderId) {
		log.info("Fetching order. orderId={}", orderId);
		Order order = orderRepository.findById(orderId)
		        .orElseThrow(() -> new OrderNotFoundException(orderId));

		log.debug("Order found. orderId={}, status={}",
		        order.getOrderId(),
		        order.getOrderStatus());

		return order;
	}

	@Override
	public List<Order> getOrdersByEmail(String email) {
		log.info("Fetching orders for customerId={}", email);
		List<Order> orders = orderRepository.findAll(
	            Sort.by(Sort.Direction.DESC, "createdAt"));
		log.info("Fetched {} orders for customerId={}",
		        orders.size(),
		        email);
		System.out.println("********** Orders + "+orders.toString());
		return orders;
	}

	@Override
	public List<Order> getAllOrders() {

		log.info("Fetching all orders.");

		List<Order> orders = orderRepository.findAll();

		log.info("Fetched {} orders.", orders.size());

		return orders;
	}

	@Override
	public void cancelOrder(Long orderId) {
		log.info("Cancelling order. orderId={}", orderId);
		Order order = getOrderById(orderId);
		log.debug("Current order status. orderId={}, status={}", orderId, order.getOrderStatus());
		if (order.getOrderStatus() == OrderStatus.DELIVERED || order.getOrderStatus() == OrderStatus.SHIPPED
				|| order.getOrderStatus() == OrderStatus.PACKED || order.getOrderStatus() == OrderStatus.CANCELLED) {

			throw new InvalidOrderException("Order cannot be cancelled");
		}

		order.setOrderStatus(OrderStatus.CANCELLED);
		log.info("Order cancelled successfully. orderId={}, orderNumber={}", order.getOrderId(),
				order.getOrderNumber());
		orderRepository.save(order);
	}

	private void validateProduct(ProductInternalResponse product, OrderItemRequest dto) {

		log.debug("Validating product. productId={}", dto.getProductId());

		if (product == null) {
			log.error("Product not found. productId={}", dto.getProductId());
			throw new InvalidOrderException("Product Not Found");
		}

		
		if (product.getAvailableQuantity() < dto.getQuantity()) {
			log.error("Insufficient stock. productId={}, requested={}, available={}", product.getId(),
					dto.getQuantity(), product.getAvailableQuantity());

			throw new InvalidOrderException("Insufficient stock");
		}

		log.debug("Product validation successful. productId={}", product.getId());
	}
	
	
	private String generateOrderNumber() {

	    String timestamp = LocalDateTime.now()
	            .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

	    int random = ThreadLocalRandom.current()
	            .nextInt(1000, 9999);

	    return "ORD-" + timestamp + "-" + random;
	}
}