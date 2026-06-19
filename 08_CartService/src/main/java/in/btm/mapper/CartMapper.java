package in.btm.mapper;

import in.btm.dto.CartItemResponse;
import in.btm.dto.CartResponse;
import in.btm.entity.Cart;
import in.btm.entity.CartItem;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CartMapper {

    // Cart -> CartResponse
    @Mapping(source = "items", target = "items")
    CartResponse toResponse(Cart cart);

    // CartItem -> CartItemResponse
    @Mapping(source = "productId", target = "productId")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "imageUrl", target = "imageUrl")
    @Mapping(source = "price", target = "price")
    @Mapping(source = "quantity", target = "quantity")
    @Mapping(source = "subTotal", target = "subTotal")
    CartItemResponse toItemResponse(CartItem item);

    List<CartItemResponse> toItemResponseList(List<CartItem> items);
}