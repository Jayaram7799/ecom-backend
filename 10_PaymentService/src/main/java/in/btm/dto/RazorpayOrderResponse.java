package in.btm.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RazorpayOrderResponse {

    private String razorpayOrderId;

    private String key;

    private BigDecimal amount;

    private String currency;
}