package in.btm.dto;

import lombok.Data;

@Data
public class OrderItemRequest {

	private Integer productId;

    private Integer quantity;
}