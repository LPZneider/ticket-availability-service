package co.com.nequi.api.dto;

import java.time.Instant;
import java.util.List;

public record OrderStatusResponse(
        String orderId,
        String eventId,
        List<String> ticketIds,
        String userId,
        String orderStatus,
        Instant createdAt
) {
}
