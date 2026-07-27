package in.btm.service;

import java.util.List;

import in.btm.dto.CreateOrderRequest;
import in.btm.dto.OrderDetailsResponse;
import in.btm.dto.OrderResponse;
import in.btm.entity.Order;

public interface OrderService {

	OrderResponse createOrder(CreateOrderRequest request, String email);

	Order getOrderById(Long orderId);

	List<Order> getOrdersByEmail(String email);

	List<Order> getAllOrders();

	void cancelOrder(Long orderId);

	public OrderDetailsResponse getOrderDetails(Long orderId);
}