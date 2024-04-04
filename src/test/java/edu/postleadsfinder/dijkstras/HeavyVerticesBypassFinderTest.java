package edu.postleadsfinder.dijkstras;

import edu.postleadsfinder.*;

public class HeavyVerticesBypassFinderTest extends AbstractDominatorsFinderTest<DijPayload> {

    private final AlgorithmHelper<DijPayload> algorithmHelper = new HeavyBypassAlgorithmHelper();

    @Override
    protected AlgorithmHelper<DijPayload> getFactory() {
        return algorithmHelper;
    }
}
