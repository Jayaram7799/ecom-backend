package in.btm.exceptions;

public class InvalidTemporaryPasswordException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidTemporaryPasswordException(String message) {
        super(message);
    }
}
