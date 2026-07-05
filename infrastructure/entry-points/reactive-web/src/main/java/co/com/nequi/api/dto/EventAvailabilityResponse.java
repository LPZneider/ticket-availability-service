package co.com.nequi.api.dto;

import java.util.Map;

public record EventAvailabilityResponse(
        String eventId,
        String eventName,
        long totalTickets,
        long availableCount,
        Map<String, Long> ticketsByStatus
) {
}
