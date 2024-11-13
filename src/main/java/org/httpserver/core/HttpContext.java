package org.httpserver.core;

import java.util.function.Consumer;

public record HttpContext(String path, Consumer<HttpExchange> handler) {

    public boolean matchesPath(String requestPath) {
        // Check if the request path starts with the base path (e.g., "/" matches "/users")
        return requestPath.startsWith(path);
    }

}
