package co.com.nequi.model.exception;

public class OrderNotFoundException extends ResourceNotFoundException {

    public OrderNotFoundException(String orderId) {
        super("Order not found: " + orderId);
    }
}
