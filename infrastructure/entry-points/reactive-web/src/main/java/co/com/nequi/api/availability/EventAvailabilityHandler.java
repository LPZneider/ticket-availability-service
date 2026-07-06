package co.com.nequi.api.availability;

import co.com.nequi.api.dto.EventAvailabilityResponse;
import co.com.nequi.api.dto.response.ApiResponse;
import co.com.nequi.api.dto.response.StatusResponseBodyApi;
import co.com.nequi.api.util.enums.TechnicalMessage;
import co.com.nequi.api.validator.HeaderValidator;
import co.com.nequi.api.validator.PathVariableValidator;
import co.com.nequi.model.availability.EventAvailability;
import co.com.nequi.model.exception.EventNotFoundException;
import co.com.nequi.usecase.availability.GetEventAvailabilityUseCase;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static co.com.nequi.api.util.constant.HandlerConstantsApi.HEADER_MESSAGE_ID;
import static co.com.nequi.api.util.constant.HandlerConstantsApi.HEADER_REGION;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventAvailabilityHandler {

    private static final String CIRCUIT_BREAKER_NAME = "getEventAvailability";
    private static final String FALLBACK_METHOD = "fallback";
    private static final String TAG_STATUS = "status";

    private final GetEventAvailabilityUseCase getEventAvailabilityUseCase;
    private final MeterRegistry meterRegistry;

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = FALLBACK_METHOD)
    public Mono<ServerResponse> handle(ServerRequest request) {
        String messageId = request.headers().firstHeader(HEADER_MESSAGE_ID);
        String region = request.headers().firstHeader(HEADER_REGION);
        String eventId = request.pathVariable("eventId");

        return HeaderValidator.headers(messageId, region)
                .filter(errors -> !errors.isEmpty())
                .flatMap(errors -> {
                    log.warn("[AVAILABILITY] Header validation failed | messageId={}, errors={}", messageId, errors);
                    return buildBadRequest(messageId, errors);
                })
                .switchIfEmpty(Mono.defer(() -> PathVariableValidator.validate(eventId, "eventId")
                        .filter(errors -> !errors.isEmpty())
                        .flatMap(errors -> {
                            log.warn("[AVAILABILITY] Path validation failed | messageId={}, errors={}", messageId, errors);
                            return buildBadRequest(messageId, errors);
                        })
                        .switchIfEmpty(Mono.defer(() -> processValidRequest(eventId, messageId, region)))))
                .onErrorResume(EventNotFoundException.class, ex -> {
                    log.warn("[AVAILABILITY] Event not found | messageId={}, eventId={}", messageId, eventId);
                    return ServerResponse.status(HttpStatus.NOT_FOUND)
                            .bodyValue(errorResponse(messageId, HttpStatus.NOT_FOUND, TechnicalMessage.EVENT_NOT_FOUND));
                })
                .onErrorResume(ex -> {
                    log.error("[AVAILABILITY] Unexpected error | messageId={}, message={}", messageId, ex.getMessage(), ex);
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .bodyValue(errorResponse(messageId, HttpStatus.INTERNAL_SERVER_ERROR, TechnicalMessage.ERROR_INTERNAL_SERVER));
                });
    }

    public Mono<ServerResponse> fallback(ServerRequest request, Exception exception) {
        String messageId = request.headers().firstHeader(HEADER_MESSAGE_ID);
        log.error("[AVAILABILITY] Fallback triggered | messageId={}, exception={}, message={}",
                messageId, exception.getClass().getSimpleName(), exception.getMessage(), exception);
        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .bodyValue(errorResponse(messageId, HttpStatus.INTERNAL_SERVER_ERROR, TechnicalMessage.ERROR_INTERNAL_SERVER));
    }

    public Mono<ServerResponse> fallback(ServerRequest request, CallNotPermittedException exception) {
        String messageId = request.headers().firstHeader(HEADER_MESSAGE_ID);
        log.error("[AVAILABILITY] Circuit breaker OPEN | messageId={}, message={}", messageId, exception.getMessage());
        return ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                .bodyValue(errorResponse(messageId, HttpStatus.SERVICE_UNAVAILABLE, TechnicalMessage.ERROR_SERVICE_UNAVAILABLE));
    }

    private Mono<ServerResponse> processValidRequest(String eventId, String messageId, String region) {
        Timer.Sample sample = Timer.start(meterRegistry);
        return getEventAvailabilityUseCase.getByEventId(eventId)
                .doOnSuccess(availability -> {
                    sample.stop(Timer.builder("availability.get.duration").tag(TAG_STATUS, "success").register(meterRegistry));
                    meterRegistry.counter("availability.get", TAG_STATUS, "success").increment();
                })
                .doOnError(e -> {
                    sample.stop(Timer.builder("availability.get.duration").tag(TAG_STATUS, "error").register(meterRegistry));
                    meterRegistry.counter("availability.get", TAG_STATUS, "error").increment();
                })
                .flatMap(availability -> ServerResponse.ok().bodyValue(
                        ApiResponse.builder()
                                .code(HttpStatus.OK.value())
                                .description(HttpStatus.OK.getReasonPhrase())
                                .messageId(messageId)
                                .region(region)
                                .data(toResponse(availability))
                                .build()));
    }

    private Mono<ServerResponse> buildBadRequest(String messageId, List<StatusResponseBodyApi> errors) {
        return ServerResponse.badRequest().bodyValue(
                ApiResponse.builder()
                        .code(HttpStatus.BAD_REQUEST.value())
                        .description(HttpStatus.BAD_REQUEST.getReasonPhrase())
                        .messageId(messageId)
                        .errors(errors)
                        .build());
    }

    private static ApiResponse errorResponse(String messageId, HttpStatus status, TechnicalMessage tm) {
        return ApiResponse.builder()
                .code(status.value())
                .description(status.getReasonPhrase())
                .messageId(messageId)
                .errors(List.of(StatusResponseBodyApi.builder()
                        .code(tm.getCode())
                        .message(tm.getMessage())
                        .system(tm.getSystem())
                        .build()))
                .build();
    }

    private static EventAvailabilityResponse toResponse(EventAvailability availability) {
        return new EventAvailabilityResponse(
                availability.eventId(),
                availability.eventName(),
                availability.totalTickets(),
                availability.availableCount(),
                Map.of());
    }
}
