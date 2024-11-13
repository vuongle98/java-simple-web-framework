package org.simpleframework.core;


public class Application {

    public static void run(Class<?> applicationClasses, String[] args) {

        ComponentScanner.scanAndRegisterComponents(applicationClasses);

        HttpServer.start(8080);
        System.out.println("Application started on http://localhost:8080");
    }
}
