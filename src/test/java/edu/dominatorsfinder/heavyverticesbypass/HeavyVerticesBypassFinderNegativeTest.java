package edu.dominatorsfinder.heavyverticesbypass;

import edu.dominatorsfinder.AbstractFinderFactory;
import edu.dominatorsfinder.NaiveDfsDominatorsFinderNegativeCasesTest;
import edu.dominatorsfinder.dijkstras.DijPayload;

public class HeavyVerticesBypassFinderNegativeTest extends NaiveDfsDominatorsFinderNegativeCasesTest<DijPayload> {
    @Override
    protected AbstractFinderFactory<DijPayload> getFactory() {
        return new HeavyBypassFinderFactory();
    }
}
