package co.com.nequi.usecase.availability;

import co.com.nequi.model.availability.EventAvailability;
import co.com.nequi.model.event.Event;
import co.com.nequi.model.event.gateways.EventRepository;
import co.com.nequi.model.exception.EventNotFoundException;
import co.com.nequi.model.ticket.Ticket;
import co.com.nequi.model.ticket.TicketStatus;
import co.com.nequi.model.ticket.gateways.TicketRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GetEventAvailabilityUseCase {

    private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;

    public Mono<EventAvailability> getByEventId(String eventId) {
        return eventRepository.findById(eventId)
                .switchIfEmpty(Mono.error(() -> new EventNotFoundException(eventId)))
                .flatMap(event -> ticketRepository.findByEventId(eventId)
                        .collect(Collectors.groupingBy(
                                Ticket::getStatus,
                                () -> new EnumMap<>(TicketStatus.class),
                                Collectors.counting()))
                        .map(countsByStatus -> toAvailability(event, countsByStatus)));
    }

    private EventAvailability toAvailability(Event event, Map<TicketStatus, Long> countsByStatus) {
        long total = countsByStatus.values().stream().mapToLong(Long::longValue).sum();
        long available = countsByStatus.getOrDefault(TicketStatus.AVAILABLE, 0L);
        return new EventAvailability(event.getEventId(), event.getName(), total, available, countsByStatus);
    }
}
