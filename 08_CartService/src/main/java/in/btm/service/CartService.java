package in.btm.service;

import java.util.List;

import in.btm.dto.CartResponse;

public interface CartService {
	CartResponse addItem(String email, Integer productId, Integer quantity);

	CartResponse removeItem(String email, Integer productId);

	CartResponse getCart(String email);

	void clearCart(String email);

	CartResponse incrementQuantity(String email, Integer productId);

	CartResponse decrementQuantity(String email, Integer productId);
	
	CartResponse removePurchasedItems(String email, List<Integer> productIds);
}
