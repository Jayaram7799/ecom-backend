package in.btm.service;

import java.util.List;

import in.btm.dto.CreateOrderRequest;
import in.btm.dto.OrderDetailsResponse;
import in.btm.dto.OrderResponse;
import in.btm.dto.OrderSummaryResponse;
import in.btm.entity.Order;

public interface OrderService {

	OrderResponse createOrder(CreateOrderRequest request, String email);

	Order getOrderById(Long orderId);

	OrderDetailsResponse getOrderDetails(Long orderId, String email);

	List<OrderSummaryResponse> getOrdersByEmail(String email);

	List<Order> getAllOrders();

	void cancelOrder(Long orderId, String email);
}