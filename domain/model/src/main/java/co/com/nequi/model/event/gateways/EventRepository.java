package co.com.nequi.model.event.gateways;

import co.com.nequi.model.event.Event;
import reactor.core.publisher.Mono;

public interface EventRepository {

    Mono<Event> findById(String eventId);
}
