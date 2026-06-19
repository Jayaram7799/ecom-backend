package in.btm.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class AddToCartRequest {

    private String email;
    private String productId;

    @Min(1)
    private Integer quantity;
}