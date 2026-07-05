package co.com.nequi.api.order;

import co.com.nequi.api.RouterRest;
import co.com.nequi.api.availability.EventAvailabilityHandler;
import co.com.nequi.model.exception.OrderNotFoundException;
import co.com.nequi.model.order.Order;
import co.com.nequi.model.order.OrderStatus;
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

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {RouterRest.class, EventAvailabilityHandler.class, OrderStatusHandler.class})
@Import(OrderStatusHandlerTest.MeterRegistryTestConfig.class)
@WebFluxTest
class OrderStatusHandlerTest {

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
    void shouldReturnOrderStatusWhenOrderExists() {
        Order order = Order.builder()
                .orderId("order-1")
                .eventId("event-1")
                .ticketIds(List.of("t1", "t2"))
                .userId("user-1")
                .orderStatus(OrderStatus.CONFIRMED)
                .createdAt(Instant.parse("2026-07-01T10:05:00Z"))
                .build();

        when(getOrderStatusUseCase.getByOrderId("order-1")).thenReturn(Mono.just(order));

        webTestClient.get()
                .uri("/api/v1/orders/order-1/status")
                .header("messageId", "msg-1")
                .header("region", "C001")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.orderId").isEqualTo("order-1")
                .jsonPath("$.data.orderStatus").isEqualTo("CONFIRMED");
    }

    @Test
    void shouldReturn400WhenHeadersAreMissing() {
        webTestClient.get()
                .uri("/api/v1/orders/order-1/status")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturnNotFoundWhenOrderDoesNotExist() {
        when(getOrderStatusUseCase.getByOrderId("missing"))
                .thenReturn(Mono.error(new OrderNotFoundException("missing")));

        webTestClient.get()
                .uri("/api/v1/orders/missing/status")
                .header("messageId", "msg-1")
                .header("region", "C001")
                .exchange()
                .expectStatus().isNotFound();
    }
}
