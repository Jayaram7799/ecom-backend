package in.btm.dto;


import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateCartRequest {

    private String userId;
    private Integer productId;

    @Min(0)
    private Integer quantity;
}