package co.com.nequi.usecase.availability;

import co.com.nequi.model.availability.EventAvailability;
import co.com.nequi.model.event.gateways.EventRepository;
import co.com.nequi.model.exception.EventNotFoundException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class GetEventAvailabilityUseCase {

    private final EventRepository eventRepository;

    public Mono<EventAvailability> getByEventId(String eventId) {
        return eventRepository.findById(eventId)
                .switchIfEmpty(Mono.error(() -> new EventNotFoundException(eventId)))
                .map(event -> new EventAvailability(
                        event.getEventId(),
                        event.getName(),
                        event.getTotalCapacity(),
                        event.getAvailableCount(),
                        null));
    }
}
