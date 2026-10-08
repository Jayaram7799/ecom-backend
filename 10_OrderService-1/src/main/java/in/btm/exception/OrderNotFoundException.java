package in.btm.exception;



public class  OrderNotFoundException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public  OrderNotFoundException(String orderId) {
        super("Order not found with id : " + orderId);
    }
}