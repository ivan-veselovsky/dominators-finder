package edu.postleadsfinder;

import java.util.List;

public interface IDominatorsFinder<Payload> {

    List<Vertex<Payload>> computeDominators();
}
