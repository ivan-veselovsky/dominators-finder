package edu.postleadsfinder;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.then;

class AssertionsEnabledTest {
    @Test
    void ensure_assertions_enabled() {
        then(Util.areAssertionsEnabled()).describedAs("Assertions must be enabled in tests.").isTrue();
    }
}
