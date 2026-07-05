package co.com.nequi.usecase.order;

import co.com.nequi.model.exception.OrderNotFoundException;
import co.com.nequi.model.order.Order;
import co.com.nequi.model.order.gateways.OrderRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class GetOrderStatusUseCase {

    private final OrderRepository orderRepository;

    public Mono<Order> getByOrderId(String orderId) {
        return orderRepository.findLatestByOrderId(orderId)
                .switchIfEmpty(Mono.error(() -> new OrderNotFoundException(orderId)));
    }
}
