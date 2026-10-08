package in.btm.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import in.btm.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderSummaryResponse {

    private Long orderId;

    private String orderNumber;

    private BigDecimal totalAmount;

    private Integer totalQuantity;

    private String customerEmail;

    private OrderStatus orderStatus;

    private Integer shippingAddressId;

    private String paymentStatus;

    private String razorpayOrderId;

    private String razorpayPaymentId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<OrderItemResponse> items;
}