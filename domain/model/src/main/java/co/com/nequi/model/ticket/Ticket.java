package co.com.nequi.model.ticket;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class Ticket {
    String ticketId;
    String eventId;
    String orderId;
    TicketStatus status;
    long version;
    Instant reservedAt;
    Instant reservationExpiresAt;
}
