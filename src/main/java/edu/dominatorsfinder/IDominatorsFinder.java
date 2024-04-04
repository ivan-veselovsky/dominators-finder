package edu.dominatorsfinder;

import java.util.List;

public interface IDominatorsFinder<Payload> {

    List<Vertex<Payload>> computeDominators();
}
