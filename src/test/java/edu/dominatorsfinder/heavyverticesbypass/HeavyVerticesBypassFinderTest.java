package edu.dominatorsfinder.heavyverticesbypass;

import edu.dominatorsfinder.*;
import edu.dominatorsfinder.dijkstras.DijPayload;

public class HeavyVerticesBypassFinderTest extends AbstractDominatorsFinderTest<DijPayload> {

    private final AbstractFinderFactory<DijPayload> algorithmHelper = new HeavyBypassFinderFactory();

    @Override
    protected AbstractFinderFactory<DijPayload> getFactory() {
        return algorithmHelper;
    }
}
