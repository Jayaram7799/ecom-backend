package in.btm.exceptions;

public class PaymentNotReadyException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PaymentNotReadyException(Long orderId) {
        super("Payment is still being initialized for order : " + orderId);
    }
}