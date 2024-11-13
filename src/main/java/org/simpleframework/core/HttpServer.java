package org.simpleframework.core;

public class HttpServer {

    public static void start(int port) {
        org.httpserver.core.HttpServer server = new org.httpserver.core.HttpServer(port, 10);
        server.createContext("/", Dispatcher::handle);
        server.start();
    }
}
