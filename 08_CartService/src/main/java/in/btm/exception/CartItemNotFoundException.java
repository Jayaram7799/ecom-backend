package in.btm.exception;

import java.io.Serial;

public class CartItemNotFoundException extends RuntimeException {

    /**
     * 
     */
    @Serial
    private static final long serialVersionUID = 1L;

	public CartItemNotFoundException(Integer productId) {
        super("Item not found in cart. Product Id: " + productId);
    }
}