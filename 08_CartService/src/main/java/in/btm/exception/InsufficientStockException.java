package in.btm.exception;

import java.io.Serial;


public class InsufficientStockException extends RuntimeException {

    /**
     * 
     */
    @Serial
    private static final long serialVersionUID = 1L;

	public InsufficientStockException(String productName, Integer available, Integer requested) {
        super("Insufficient stock for product '%s'. Available: %d, Requested: %d".formatted(
                productName,
                available,
                requested
        ));
    }
}