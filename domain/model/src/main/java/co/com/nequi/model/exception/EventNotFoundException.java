package co.com.nequi.model.exception;

public class EventNotFoundException extends ResourceNotFoundException {

    public EventNotFoundException(String eventId) {
        super("Event not found: " + eventId);
    }
}
