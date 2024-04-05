package edu.dominatorsfinder.naivedfs;

import edu.dominatorsfinder.AbstractFinderFactory;
import edu.dominatorsfinder.AbstractDominatorsFinderNegativeCasesTest;

public class NaiveDfsDominatorsFinderNegativeTest extends AbstractDominatorsFinderNegativeCasesTest<DfsPayload> {

    @Override
    protected AbstractFinderFactory<DfsPayload> getFactory() {
        return new NaiveDfsFinderFactory();
    }
}
