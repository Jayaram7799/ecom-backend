package in.btm.dto;


import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;


import java.time.LocalDateTime;
import java.util.List;


@Data
@Builder
public class OrderDetailsResponse {

    private Long orderId;

    private String orderNumber;

    private BigDecimal totalAmount;

    private Integer totalQuantity;

    private String orderStatus;

    private String paymentStatus;

    private String razorpayOrderId;

    private String razorpayPaymentId;

    private LocalDateTime createdAt;

    private List<OrderItemResponse> items;
}