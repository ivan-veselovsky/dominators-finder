package edu.postleadsfinder.dijkstras;

import edu.postleadsfinder.*;
import edu.postleadsfinder.heavyverticesbypass.HeavyBypassFinderFactory;

public class HeavyVerticesBypassFinderTest extends AbstractDominatorsFinderTest<DijPayload> {

    private final AbstractFinderFactory<DijPayload> algorithmHelper = new HeavyBypassFinderFactory();

    @Override
    protected AbstractFinderFactory<DijPayload> getFactory() {
        return algorithmHelper;
    }
}
