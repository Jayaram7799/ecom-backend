package in.btm.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderItemResponse {

	private Long id;

    private Long productId;

    private String productName;
    
    private String imageUrl;

    private Integer quantity;

    private BigDecimal price;

    private BigDecimal subtotal;
}