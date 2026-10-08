package in.btm.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class OrderResponse {

    private Long orderId;

    private String orderNumber;

    private String status;

    private String message;
}