package co.com.nequi.api.availability;

import co.com.nequi.api.RouterRest;
import co.com.nequi.api.order.OrderStatusHandler;
import co.com.nequi.model.availability.EventAvailability;
import co.com.nequi.model.exception.EventNotFoundException;
import co.com.nequi.model.ticket.TicketStatus;
import co.com.nequi.usecase.availability.GetEventAvailabilityUseCase;
import co.com.nequi.usecase.order.GetOrderStatusUseCase;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {RouterRest.class, EventAvailabilityHandler.class, OrderStatusHandler.class})
@Import(EventAvailabilityHandlerTest.MeterRegistryTestConfig.class)
@WebFluxTest
class EventAvailabilityHandlerTest {

    @TestConfiguration
    static class MeterRegistryTestConfig {
        @Bean
        MeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }
    }

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private GetEventAvailabilityUseCase getEventAvailabilityUseCase;

    @MockitoBean
    private GetOrderStatusUseCase getOrderStatusUseCase;

    @Test
    void shouldReturnAvailabilityWhenEventExists() {
        EventAvailability availability = new EventAvailability(
                "event-1", "Concert", 3L, 2L, Map.of(TicketStatus.AVAILABLE, 2L, TicketStatus.SOLD, 1L));

        when(getEventAvailabilityUseCase.getByEventId("event-1")).thenReturn(Mono.just(availability));

        webTestClient.get()
                .uri("/api/v1/events/event-1/availability")
                .header("messageId", "msg-1")
                .header("region", "C001")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.code").isEqualTo(200)
                .jsonPath("$.data.eventId").isEqualTo("event-1")
                .jsonPath("$.data.totalTickets").isEqualTo(3)
                .jsonPath("$.data.availableCount").isEqualTo(2);
    }

    @Test
    void shouldReturn400WhenHeadersAreMissing() {
        webTestClient.get()
                .uri("/api/v1/events/event-1/availability")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors.length()").isEqualTo(2);
    }

    @Test
    void shouldReturnNotFoundWhenEventDoesNotExist() {
        when(getEventAvailabilityUseCase.getByEventId("missing"))
                .thenReturn(Mono.error(new EventNotFoundException("missing")));

        webTestClient.get()
                .uri("/api/v1/events/missing/availability")
                .header("messageId", "msg-1")
                .header("region", "C001")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldReturnInternalServerErrorAndHideDetailsOnUnexpectedFailure() {
        when(getEventAvailabilityUseCase.getByEventId("event-1"))
                .thenReturn(Mono.error(new IllegalStateException("dynamodb connection reset by peer")));

        webTestClient.get()
                .uri("/api/v1/events/event-1/availability")
                .header("messageId", "msg-1")
                .header("region", "C001")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.errors[0].message").isEqualTo("Unexpected error, please contact support");
    }
}
