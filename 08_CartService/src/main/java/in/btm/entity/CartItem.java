package in.btm.entity;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItem {

    private Integer productId;
    private String name;
    private String imageUrl;

    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subTotal;
}
