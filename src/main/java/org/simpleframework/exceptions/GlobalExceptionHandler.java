package org.simpleframework.exceptions;

public class GlobalExceptionHandler {

    public static void handleException(Throwable e) {
        System.out.println("Global Exception: " + e.getMessage());
    }
}
