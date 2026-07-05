package co.com.nequi.usecase.availability;

import co.com.nequi.model.event.Event;
import co.com.nequi.model.event.gateways.EventRepository;
import co.com.nequi.model.exception.EventNotFoundException;
import co.com.nequi.model.ticket.Ticket;
import co.com.nequi.model.ticket.TicketStatus;
import co.com.nequi.model.ticket.gateways.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetEventAvailabilityUseCaseTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private TicketRepository ticketRepository;

    private GetEventAvailabilityUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetEventAvailabilityUseCase(eventRepository, ticketRepository);
    }

    @Test
    void shouldAggregateTicketCountsByStatus() {
        Event event = Event.builder().eventId("event-1").name("Concert").venue("Arena")
                .date(Instant.parse("2026-08-01T20:00:00Z")).totalCapacity(100).build();

        when(eventRepository.findById("event-1")).thenReturn(Mono.just(event));
        when(ticketRepository.findByEventId("event-1")).thenReturn(Flux.just(
                ticket("t1", TicketStatus.AVAILABLE),
                ticket("t2", TicketStatus.AVAILABLE),
                ticket("t3", TicketStatus.SOLD)));

        StepVerifier.create(useCase.getByEventId("event-1"))
                .expectNextMatches(availability ->
                        availability.eventId().equals("event-1")
                                && availability.totalTickets() == 3
                                && availability.availableCount() == 2
                                && availability.countsByStatus().get(TicketStatus.SOLD) == 1L)
                .verifyComplete();
    }

    @Test
    void shouldReturnZeroCountsWhenEventHasNoTickets() {
        Event event = Event.builder().eventId("event-2").name("Play").venue("Theatre")
                .date(Instant.parse("2026-09-01T20:00:00Z")).totalCapacity(50).build();

        when(eventRepository.findById("event-2")).thenReturn(Mono.just(event));
        when(ticketRepository.findByEventId("event-2")).thenReturn(Flux.empty());

        StepVerifier.create(useCase.getByEventId("event-2"))
                .expectNextMatches(availability -> availability.totalTickets() == 0 && availability.availableCount() == 0)
                .verifyComplete();
    }

    @Test
    void shouldFailWithEventNotFoundWhenEventDoesNotExist() {
        when(eventRepository.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.getByEventId("missing"))
                .expectError(EventNotFoundException.class)
                .verify();
    }

    private static Ticket ticket(String ticketId, TicketStatus status) {
        return Ticket.builder()
                .ticketId(ticketId)
                .eventId("event-1")
                .orderId(null)
                .status(status)
                .version(1L)
                .reservedAt(Instant.parse("2026-07-01T10:00:00Z"))
                .build();
    }
}
