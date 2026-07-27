package in.btm.mapper;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import in.btm.dto.OrderDetailsResponse;
import in.btm.dto.OrderItemRequest;
import in.btm.dto.OrderItemResponse;
import in.btm.dto.ProductInternalResponse;
import in.btm.entity.Order;
import in.btm.entity.OrderItem;

@Component
public class OrderMapper {

	public OrderItem toOrderItem(OrderItemRequest dto, ProductInternalResponse product) {

		OrderItem item = new OrderItem();

		item.setProductId(product.getId().longValue());

		item.setProductName(product.getName());

		item.setQuantity(dto.getQuantity());

		item.setUnitPrice(product.getPrice());

		item.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity())));

		return item;
	}

	public OrderDetailsResponse toOrderDetails(Order order) {

		return OrderDetailsResponse.builder().orderId(order.getOrderId()).orderNumber(order.getOrderNumber())
				.totalAmount(order.getTotalAmount()).totalQuantity(order.getTotalQuantity())
				.orderStatus(order.getOrderStatus().name()).paymentStatus(order.getPaymentStatus().name())
				.razorpayOrderId(order.getRazorpayOrderId()).razorpayPaymentId(order.getRazorpayPaymentId())
				.createdAt(order.getCreatedAt())
				.items(order.getOrderItems().stream().map(this::toOrderItemResponse).toList()).build();
	}

	private OrderItemResponse toOrderItemResponse(OrderItem item) {

		return OrderItemResponse.builder().productId(item.getProductId()).productName(item.getProductName())
				.quantity(item.getQuantity()).unitPrice(item.getUnitPrice()).totalPrice(item.getTotalPrice()).build();
	}

}