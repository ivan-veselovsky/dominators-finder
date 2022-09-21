package edu.postleadsfinder;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

class AssertionsEnabledTest {
    @Test
    void ensure_assertions_enabled() {
        try {
            assert false;
        } catch (AssertionError ae) {
            return;
        }
        fail("Assertions seems to be off. ");
    }
}
