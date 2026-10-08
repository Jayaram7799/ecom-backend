package in.btm.dto;


import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;


import java.time.LocalDateTime;
import java.util.List;

import in.btm.enums.OrderStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;


@Data
@Builder
public class OrderDetailsResponse {

    private Long orderId;

    private String orderNumber;

    private BigDecimal totalAmount;

    private Integer totalQuantity;

    private String customerEmail;
    
    @Enumerated(EnumType.STRING)
	
	private OrderStatus orderStatus;
    
    private Integer shippingAddressId;

    private String paymentStatus;

    private String razorpayOrderId;

    private String razorpayPaymentId;

    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;

    private List<OrderItemResponse> items;
}