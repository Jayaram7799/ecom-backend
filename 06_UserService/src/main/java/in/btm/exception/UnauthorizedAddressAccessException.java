package in.btm.exception;

public class UnauthorizedAddressAccessException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UnauthorizedAddressAccessException(String message) {
        super(message);
    }
}