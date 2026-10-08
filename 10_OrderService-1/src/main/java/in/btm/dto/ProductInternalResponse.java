package in.btm.dto;


import java.math.BigDecimal;

import lombok.Data;

@Data
public class ProductInternalResponse {

    private Integer id;

    private String name;

    private BigDecimal price;

    private Integer availableQuantity;
    
    private String imageUrl;

    private Boolean active;
}