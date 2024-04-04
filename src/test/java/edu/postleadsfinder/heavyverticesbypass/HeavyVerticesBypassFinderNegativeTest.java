package edu.postleadsfinder.heavyverticesbypass;

import edu.postleadsfinder.AbstractFinderFactory;
import edu.postleadsfinder.NaiveDfsDominatorsFinderNegativeCasesTest;
import edu.postleadsfinder.dijkstras.DijPayload;

public class HeavyVerticesBypassFinderNegativeTest extends NaiveDfsDominatorsFinderNegativeCasesTest<DijPayload> {
    @Override
    protected AbstractFinderFactory<DijPayload> getFactory() {
        return new HeavyBypassFinderFactory();
    }
}
