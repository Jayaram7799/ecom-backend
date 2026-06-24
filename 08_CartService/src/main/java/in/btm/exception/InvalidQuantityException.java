package in.btm.exception;

import java.io.Serial;


public class InvalidQuantityException extends RuntimeException {

    /**
     * 
     */
    @Serial
    private static final long serialVersionUID = 1L;

	public InvalidQuantityException() {
        super("Quantity must be greater than zero");
    }
}