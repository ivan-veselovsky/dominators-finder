package edu.dominatorsfinder.naivedfs;

import edu.dominatorsfinder.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.assertj.core.api.BDDAssertions.thenThrownBy;

public class NaiveDfsDominatorsFinderPositiveTest extends AbstractDominatorsFinderPositiveTest<DfsPayload> {

    private final AbstractFinderFactory<DfsPayload> algorithmHelper = new NaiveDfsFinderFactory();

    @Override
    protected AbstractFinderFactory<DfsPayload> getFactory() {
        return algorithmHelper;
    }

    @Override
    @ParameterizedTest(name = "{0}, {1} -> {2}")
    @MethodSource("testCases")
    protected void doTest(AbstractDominatorsFinderPositiveTest.TestGraph testGraph, String startVertexKey, String exitVertexKey, List<String> expectedDominators) {
        // NB: intentionally expect failure there for the naive algorithm:
        if (testGraph.name().startsWith("bug_in_naive_algorithm")) {
            thenThrownBy(() -> super.doTest(testGraph, startVertexKey, exitVertexKey, expectedDominators))
                    .isInstanceOf(AssertionError.class);
        } else {
            super.doTest(testGraph, startVertexKey, exitVertexKey, expectedDominators);
        }
    }
}
