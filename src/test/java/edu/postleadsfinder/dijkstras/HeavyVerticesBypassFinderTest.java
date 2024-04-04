package edu.postleadsfinder.dijkstras;

import edu.postleadsfinder.*;

public class HeavyVerticesBypassFinderTest extends AbstractDominatorsFinderTest<DijPayload> {

    private final AbstractFinderFactory<DijPayload> algorithmHelper = new HeavyBypassFinderFactory();

    @Override
    protected AbstractFinderFactory<DijPayload> getFactory() {
        return algorithmHelper;
    }
}
