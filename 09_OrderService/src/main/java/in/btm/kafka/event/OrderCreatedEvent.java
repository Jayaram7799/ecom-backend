package in.btm.kafka.event;

import java.math.BigDecimal;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {

    private Long orderId;

    private Integer customerId;

    private BigDecimal totalAmount;
}