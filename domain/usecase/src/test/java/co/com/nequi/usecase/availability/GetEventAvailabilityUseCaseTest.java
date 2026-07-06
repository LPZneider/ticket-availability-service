package co.com.nequi.usecase.availability;

import co.com.nequi.model.availability.EventAvailability;
import co.com.nequi.model.event.Event;
import co.com.nequi.model.event.gateways.EventRepository;
import co.com.nequi.model.exception.EventNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetEventAvailabilityUseCaseTest {

    @Mock private EventRepository eventRepository;

    private GetEventAvailabilityUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetEventAvailabilityUseCase(eventRepository);
    }

    private static Event event(int total, int available) {
        return Event.builder().eventId("event-1").name("Concert").venue("Arena")
                .date(Instant.parse("2026-08-01T20:00:00Z"))
                .totalCapacity(total).availableCount(available).build();
    }

    @Test
    void shouldReturnAvailableCountDirectlyFromEvent() {
        when(eventRepository.findById("event-1")).thenReturn(Mono.just(event(100, 75)));

        StepVerifier.create(useCase.getByEventId("event-1"))
                .assertNext(availability -> {
                    assertThat(availability.eventId()).isEqualTo("event-1");
                    assertThat(availability.eventName()).isEqualTo("Concert");
                    assertThat(availability.totalTickets()).isEqualTo(100);
                    assertThat(availability.availableCount()).isEqualTo(75);
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnZeroAvailableWhenFullySold() {
        when(eventRepository.findById("event-1")).thenReturn(Mono.just(event(50, 0)));

        StepVerifier.create(useCase.getByEventId("event-1"))
                .assertNext(availability -> {
                    assertThat(availability.totalTickets()).isEqualTo(50);
                    assertThat(availability.availableCount()).isEqualTo(0);
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnFullAvailabilityWhenNothingSold() {
        when(eventRepository.findById("event-1")).thenReturn(Mono.just(event(200, 200)));

        StepVerifier.create(useCase.getByEventId("event-1"))
                .assertNext(availability ->
                        assertThat(availability.availableCount()).isEqualTo(availability.totalTickets()))
                .verifyComplete();
    }

    @Test
    void shouldFailWithEventNotFoundWhenEventDoesNotExist() {
        when(eventRepository.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.getByEventId("missing"))
                .expectError(EventNotFoundException.class)
                .verify();
    }

    @Test
    void shouldPropagateRepositoryError() {
        when(eventRepository.findById("event-1"))
                .thenReturn(Mono.error(new RuntimeException("DynamoDB error")));

        StepVerifier.create(useCase.getByEventId("event-1"))
                .expectError(RuntimeException.class)
                .verify();
    }
}
