package co.com.nequi.model.availability;

import co.com.nequi.model.ticket.TicketStatus;

import java.util.Map;

public record EventAvailability(
        String eventId,
        String eventName,
        long totalTickets,
        long availableCount,
        Map<TicketStatus, Long> countsByStatus
) {
}
