package in.btm.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CartItemResponse {

    private Integer productId;
    private String name;
    private String imageUrl;

    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subTotal;
}