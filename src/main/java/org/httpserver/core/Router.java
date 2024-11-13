package org.httpserver.core;

import java.util.ArrayList;
import java.util.List;

public class Router {

    private final List<HttpContext> contexts = new ArrayList<>();

    public void addContext(HttpContext context) {
        contexts.add(context);
    }

    public HttpContext findContext(String path) {
        for (HttpContext context : contexts) {
            if (context.matchesPath(path)) {
                return context;
            }
        }

        return null;
    }
}
