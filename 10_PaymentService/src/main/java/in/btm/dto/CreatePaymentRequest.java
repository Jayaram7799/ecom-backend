package in.btm.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CreatePaymentRequest {

    private Long orderId;

    private BigDecimal amount;

    private Integer customerId;
}
