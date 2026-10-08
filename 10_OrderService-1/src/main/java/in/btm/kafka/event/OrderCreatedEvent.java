package in.btm.kafka.event;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {

    private UUID eventId;

    private Long orderId;

    private String orderNumber;
    
    private String customerEmail;

    private Integer customerId;

    private BigDecimal totalAmount;

    private Integer totalQuantity;
}