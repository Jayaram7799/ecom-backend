package in.btm.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDto {

    private Integer id;
    private String name;
    private String imageUrl;
    private BigDecimal price;
    private Integer stock;
}