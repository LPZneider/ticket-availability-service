package co.com.nequi.api.config;

import co.com.nequi.api.RouterRest;
import co.com.nequi.api.availability.EventAvailabilityHandler;
import co.com.nequi.api.order.OrderStatusHandler;
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

@ContextConfiguration(classes = {RouterRest.class, EventAvailabilityHandler.class, OrderStatusHandler.class})
@WebFluxTest
@Import({CorsConfig.class, SecurityHeadersConfig.class, ConfigTest.MeterRegistryTestConfig.class})
class ConfigTest {

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
    void corsConfigurationShouldAllowOrigins() {
        webTestClient.get()
                .uri("/api/v1/events/event-1/availability")
                .exchange()
                .expectHeader().valueEquals("Content-Security-Policy",
                        "default-src 'self'; frame-ancestors 'self'; form-action 'self'")
                .expectHeader().valueEquals("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload")
                .expectHeader().valueEquals("X-Content-Type-Options", "nosniff")
                .expectHeader().valueEquals("Server", "")
                .expectHeader().valueEquals("Cache-Control", "no-store")
                .expectHeader().valueEquals("Pragma", "no-cache")
                .expectHeader().valueEquals("Referrer-Policy", "strict-origin-when-cross-origin");
    }

}
