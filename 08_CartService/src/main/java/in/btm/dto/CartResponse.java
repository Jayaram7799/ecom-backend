package in.btm.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CartResponse {

    private String email;

    private List<CartItemResponse> items;

    private BigDecimal totalPrice;

    private Integer totalItems;
}