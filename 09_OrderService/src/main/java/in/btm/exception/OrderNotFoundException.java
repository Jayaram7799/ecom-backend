package in.btm.exception;



public class OrderNotFoundException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public OrderNotFoundException(Long orderId) {
        super("Order not found with id : " + orderId);
    }
}