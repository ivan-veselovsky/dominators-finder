package edu.postleadsfinder.naivedfs;

import edu.postleadsfinder.AbstractFinderFactory;
import edu.postleadsfinder.DfsPayload;
import edu.postleadsfinder.NaiveDfsDominatorsFinderNegativeCasesTest;

public class NaiveDfsDominatorsFinderNegativeTest extends NaiveDfsDominatorsFinderNegativeCasesTest<DfsPayload> {

    @Override
    protected AbstractFinderFactory<DfsPayload> getFactory() {
        return new NaiveDfsFinderFactory();
    }
}
