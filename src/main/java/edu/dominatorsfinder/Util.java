package edu.dominatorsfinder;

import java.util.Collection;
import java.util.List;

public class Util {

    public static boolean areAssertionsEnabled() {
        try {
            assert false;
            return false;
        } catch (AssertionError ae) {
            return true;
        }
    }

    public static <P> List<String> asKeys(Collection<Vertex<P>> vertices) {
        return vertices.stream().map(Vertex::getKey).toList();
    }

}
