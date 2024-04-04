package edu.dominatorsfinder;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.fail;
import static org.assertj.core.api.BDDAssertions.then;

@Log4j2
class AssertionsEnabledTest {
    @Test
    void ensure_assertions_enabled() {
        then(Util.areAssertionsEnabled()).describedAs("Assertions must be enabled in tests.").isTrue();
    }

    @Test
    void should_not_evaluate_logging_expression_if_log_level_is_off() {
        then(log.isTraceEnabled()).isFalse();

        log.trace(() -> "foo " + someMethod("bbb"));
    }

    private String someMethod(String x) {
        System.out.println("!!! some method invoked " + x);
        fail("Should not be invoked.");
        return x + "aaa";
    }
}
