package in.btm.service;

import java.util.List;

import in.btm.dto.CreateOrderRequest;
import in.btm.dto.OrderDetailsResponse;
import in.btm.dto.OrderResponse;
import in.btm.entity.Order;

public interface OrderService {

	OrderResponse createOrder(CreateOrderRequest request, Integer userId);

	Order getOrderById(Long orderId);

	List<Order> getOrdersByCustomerId(Integer customerId);

	List<Order> getAllOrders();

	void cancelOrder(Long orderId);

	public OrderDetailsResponse getOrderDetails(Long orderId);
}