package in.btm.exception;

import java.io.Serial;


public class ProductFetchException extends RuntimeException {

    /**
     * 
     */
    @Serial
    private static final long serialVersionUID = 1L;

	public ProductFetchException(Integer productId) {
        super("Unable to fetch product details for product id: " + productId);
    }
}