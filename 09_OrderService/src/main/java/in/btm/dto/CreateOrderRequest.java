package in.btm.dto;

import java.util.List;

import lombok.Data;

@Data
public class CreateOrderRequest {

    private Integer addressId;

    private List<OrderItemRequest> items;
}