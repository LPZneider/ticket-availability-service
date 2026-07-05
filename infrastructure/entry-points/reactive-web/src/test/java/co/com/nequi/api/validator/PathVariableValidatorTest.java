package co.com.nequi.api.validator;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.Collection;

class PathVariableValidatorTest {

    @Test
    void shouldPassThroughNonBlankValue() {
        StepVerifier.create(PathVariableValidator.validate("event-1", "eventId"))
                .expectNextMatches(Collection::isEmpty)
                .verifyComplete();
    }

    @Test
    void shouldReturnErrorForBlankValue() {
        StepVerifier.create(PathVariableValidator.validate("   ", "eventId"))
                .expectNextMatches(errors -> errors.size() == 1)
                .verifyComplete();
    }
}
