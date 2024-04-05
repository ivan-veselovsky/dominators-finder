package edu.dominatorsfinder.heavyverticesbypass;

import edu.dominatorsfinder.AbstractFinderFactory;
import edu.dominatorsfinder.AbstractDominatorsFinderNegativeCasesTest;
import edu.dominatorsfinder.dijkstras.DijPayload;

public class HeavyVerticesBypassFinderNegativeTest extends AbstractDominatorsFinderNegativeCasesTest<DijPayload> {
    @Override
    protected AbstractFinderFactory<DijPayload> getFactory() {
        return new HeavyBypassFinderFactory();
    }
}
