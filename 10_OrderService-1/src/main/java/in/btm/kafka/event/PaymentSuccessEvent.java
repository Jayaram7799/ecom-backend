package in.btm.kafka.event;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSuccessEvent {

    private UUID eventId;

    private Long orderId;

    private String razorpayPaymentId;

    private LocalDateTime occurredAt;
}