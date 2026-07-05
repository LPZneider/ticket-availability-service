package co.com.nequi.usecase.order;

import co.com.nequi.model.exception.OrderNotFoundException;
import co.com.nequi.model.order.Order;
import co.com.nequi.model.order.OrderStatus;
import co.com.nequi.model.order.gateways.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetOrderStatusUseCaseTest {

    @Mock
    private OrderRepository orderRepository;

    private GetOrderStatusUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetOrderStatusUseCase(orderRepository);
    }

    @Test
    void shouldReturnLatestOrderTransitionWhenOrderExists() {
        Order order = Order.builder()
                .orderId("order-1")
                .eventId("event-1")
                .ticketIds(List.of("t1", "t2"))
                .userId("user-1")
                .orderStatus(OrderStatus.CONFIRMED)
                .createdAt(Instant.parse("2026-07-01T10:05:00Z"))
                .build();

        when(orderRepository.findLatestByOrderId("order-1")).thenReturn(Mono.just(order));

        StepVerifier.create(useCase.getByOrderId("order-1"))
                .expectNext(order)
                .verifyComplete();
    }

    @Test
    void shouldFailWithOrderNotFoundWhenOrderDoesNotExist() {
        when(orderRepository.findLatestByOrderId("missing")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.getByOrderId("missing"))
                .expectError(OrderNotFoundException.class)
                .verify();
    }
}
