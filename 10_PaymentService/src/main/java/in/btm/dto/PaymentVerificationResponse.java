package in.btm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentVerificationResponse {

    private String status;

    private String message;
}