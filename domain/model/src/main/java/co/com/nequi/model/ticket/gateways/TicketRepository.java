package co.com.nequi.model.ticket.gateways;

import co.com.nequi.model.ticket.Ticket;
import reactor.core.publisher.Flux;

public interface TicketRepository {

    Flux<Ticket> findByEventId(String eventId);
}
