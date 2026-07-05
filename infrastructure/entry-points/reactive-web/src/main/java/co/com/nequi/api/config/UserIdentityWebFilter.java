package co.com.nequi.api.config;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Upstream API Gateway + Lambda authorizer already resolved authentication and
 * forwards x-user-id / x-user-role. This filter only performs a lightweight
 * presence check as defense in depth; it does not implement authentication itself.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class UserIdentityWebFilter implements WebFilter {

    private static final String USER_ID_HEADER = "x-user-id";
    private static final String USER_ROLE_HEADER = "x-user-role";
    private static final String MISSING_IDENTITY_BODY =
            "{\"title\":\"Unauthorized\",\"status\":401,\"detail\":\"Missing required upstream identity headers\"}";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        String userId = headers.getFirst(USER_ID_HEADER);
        String userRole = headers.getFirst(USER_ROLE_HEADER);

        if (!StringUtils.hasText(userId) || !StringUtils.hasText(userRole)) {
            return rejectUnauthorized(exchange.getResponse());
        }
        return chain.filter(exchange);
    }

    private Mono<Void> rejectUnauthorized(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        DataBuffer buffer = response.bufferFactory()
                .wrap(MISSING_IDENTITY_BODY.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
