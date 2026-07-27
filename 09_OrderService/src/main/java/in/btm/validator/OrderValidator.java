package in.btm.validator;

import org.springframework.stereotype.Component;

import in.btm.dto.CreateOrderRequest;
import in.btm.dto.OrderItemRequest;
import in.btm.exception.InvalidOrderException;

@Component
public class OrderValidator {

    public void validate(CreateOrderRequest request) {

        if (request == null) {
            throw new InvalidOrderException("Request cannot be null");
        }

        if (request.getAddressId() == null) {
            throw new InvalidOrderException("Delivery address is required");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new InvalidOrderException("Cart is empty");
        }

        for (OrderItemRequest item : request.getItems()) {

            if (item.getProductId() == null) {
                throw new InvalidOrderException("Product Id is required");
            }

            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new InvalidOrderException("Quantity must be greater than zero");
            }
        }
    }
}