package in.btm.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductInternalResponse {

    private Integer id;

    private String name;

    private BigDecimal price;

    private Integer availableQuantity;

    private Boolean active;
}