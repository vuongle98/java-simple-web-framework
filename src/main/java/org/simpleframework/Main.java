package org.simpleframework;


import org.simpleframework.annotations.ComponentScan;
import org.simpleframework.core.Application;

@ComponentScan(basePackages = {"org.simpleframework"})
public class Main {
    public static void main(String[] args) throws Exception {

        Application.run(Main.class, args);
    }
}