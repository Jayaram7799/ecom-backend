package in.btm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class OrderResponse {

    private Long orderId;

    private String status;

    private String message;
}