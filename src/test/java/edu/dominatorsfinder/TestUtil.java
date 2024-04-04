package edu.dominatorsfinder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestUtil {

    static String getCallerMethodName() {
        StackTraceElement e = new Throwable().getStackTrace()[1];
        return e.getMethodName();
    }
}
