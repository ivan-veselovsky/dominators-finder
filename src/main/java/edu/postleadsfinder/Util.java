package edu.postleadsfinder;

public class Util {

    public static boolean areAssertionsEnabled() {
        try {
            assert false;
            return false;
        } catch (AssertionError ae) {
            return true;
        }
    }
}
