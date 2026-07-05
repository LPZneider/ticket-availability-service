package co.com.nequi.api;

import co.com.nequi.api.availability.EventAvailabilityHandler;
import co.com.nequi.api.dto.response.ApiResponse;
import co.com.nequi.api.order.OrderStatusHandler;
import co.com.nequi.api.util.enums.TechnicalMessage;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.info.Info;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.function.Consumer;

import static co.com.nequi.api.util.constant.HandlerConstantsApi.*;
import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;

@Configuration
@OpenAPIDefinition(info = @Info(
        title = "Ticket Availability Service",
        version = "1.0.0",
        description = "Read-only event availability and order status queries"))
public class RouterRest {

    @Bean
    public RouterFunction<ServerResponse> routerFunction(EventAvailabilityHandler eventAvailabilityHandler,
                                                           OrderStatusHandler orderStatusHandler) {
        return SpringdocRouteBuilder.route()
                .GET("/api/v1/events/{eventId}/availability", eventAvailabilityHandler::handle,
                        withHeaders("getEventAvailability", "Get Event Availability"))
                .GET("/api/v1/orders/{orderId}/status", orderStatusHandler::handle,
                        withHeaders("getOrderStatus", "Get Order Status"))
                .build();
    }

    private Consumer<Builder> withHeaders(String operationId, String summary) {
        return ops -> ops.operationId(operationId)
                .summary(summary)
                .parameter(buildParameter(HEADER_MESSAGE_ID, HEADER_MESSAGE_ID_DESCRIPTION, true))
                .parameter(buildParameter(HEADER_REGION, HEADER_REGION_DESCRIPTION, true))
                .response(buildResponse(TechnicalMessage.SUCCESS))
                .response(buildResponse(TechnicalMessage.ERROR_BAD_REQUEST))
                .response(buildResponse(TechnicalMessage.ERROR_INTERNAL_SERVER))
                .response(buildResponse(TechnicalMessage.ERROR_SERVICE_UNAVAILABLE));
    }

    private org.springdoc.core.fn.builders.parameter.Builder buildParameter(String name, String description, boolean required) {
        return parameterBuilder()
                .in(ParameterIn.HEADER)
                .name(name)
                .description(description)
                .required(required);
    }

    private org.springdoc.core.fn.builders.apiresponse.Builder buildResponse(TechnicalMessage tm) {
        return responseBuilder()
                .responseCode(String.valueOf(tm.getCodeHtp()))
                .description(tm.getMessage())
                .implementation(ApiResponse.class);
    }
}
