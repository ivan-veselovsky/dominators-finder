package edu.dominatorsfinder.naivedfs;

import edu.dominatorsfinder.AbstractFinderFactory;
import edu.dominatorsfinder.NaiveDfsDominatorsFinderNegativeCasesTest;

public class NaiveDfsDominatorsFinderNegativeTest extends NaiveDfsDominatorsFinderNegativeCasesTest<DfsPayload> {

    @Override
    protected AbstractFinderFactory<DfsPayload> getFactory() {
        return new NaiveDfsFinderFactory();
    }
}
