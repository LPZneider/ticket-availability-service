package co.com.nequi.api.validator;

import co.com.nequi.api.dto.response.StatusResponseBodyApi;
import co.com.nequi.api.util.enums.TechnicalMessage;
import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class PathVariableValidator {

    public static Mono<List<StatusResponseBodyApi>> validate(String value, String fieldName) {
        return Mono.fromCallable(() -> {
            List<StatusResponseBodyApi> errors = new ArrayList<>();
            if (!StringUtils.hasText(value)) {
                errors.add(StatusResponseBodyApi.builder()
                        .code(TechnicalMessage.ERROR_BAD_REQUEST.getCode())
                        .message(fieldName + " must not be blank")
                        .system(TechnicalMessage.ERROR_BAD_REQUEST.getSystem())
                        .build());
            }
            return errors;
        });
    }
}
