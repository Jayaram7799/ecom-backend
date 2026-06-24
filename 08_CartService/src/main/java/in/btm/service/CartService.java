package in.btm.service;

import in.btm.dto.CartResponse;

public interface CartService {
	CartResponse addItem(String email, Integer productId, Integer quantity);

	CartResponse removeItem(String email, Integer productId);

	CartResponse getCart(String email);

	void clearCart(String email);

	CartResponse incrementQuantity(String email, Integer productId);

	CartResponse decrementQuantity(String email, Integer productId);
}
