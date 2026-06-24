package in.btm.dto;

import lombok.Data;

@Data
public class OrderItemRequest {

    private Integer productId;

    private String productName;

    private Integer quantity;

    private Double price;
}