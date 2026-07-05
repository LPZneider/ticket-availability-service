package co.com.nequi.api.util.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TechnicalMessage {

    SUCCESS(200, "0", "SUCCESS"),
    ERROR_BAD_REQUEST(400, "AVL-400", "The request headers or path variables are invalid"),
    EVENT_NOT_FOUND(404, "AVL-404", "Event not found"),
    ORDER_NOT_FOUND(404, "AVL-405", "Order not found"),
    ERROR_INTERNAL_SERVER(500, "AVL-500", "Unexpected error, please contact support"),
    ERROR_SERVICE_UNAVAILABLE(503, "AVL-503", "The service is currently unable to handle the request");

    private static final String SYSTEM = "ticket-availability-service";

    private final int codeHtp;
    private final String code;
    private final String message;

    public String getSystem() {
        return SYSTEM;
    }
}
