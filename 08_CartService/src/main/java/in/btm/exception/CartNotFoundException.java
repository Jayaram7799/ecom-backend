package in.btm.exception;

import java.io.Serial;

public class CartNotFoundException extends RuntimeException {

    /**
     * 
     */
    @Serial
    private static final long serialVersionUID = 1L;

	public CartNotFoundException(String email) {
        super("Cart not found for user: " + email);
    }
}