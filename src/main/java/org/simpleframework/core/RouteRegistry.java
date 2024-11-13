package org.simpleframework.core;

import org.simpleframework.utils.RequestUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RouteRegistry {
    private static final Map<String, Method> routeHandlers = new HashMap<>();
    private static final Map<String, String> routePatterns = new HashMap<>();

    /**
     * Registers a route with a dynamic pattern, e.g., /user/{id}.
     *
     * @param httpMethod HTTP method type (GET, POST, etc.).
     * @param path       Route pattern (e.g., /user/{id}).
     * @param handler    The method that handles this route.
     */
    public static void registerRoute(String httpMethod, String path, Method handler) {
        String pattern = RequestUtils.convertToRegex(path);
        String key = httpMethod + ":" + pattern;
        routeHandlers.put(key, handler);
        routePatterns.put(key, path);
    }

    /**
     * Finds a matching route for the given HTTP method and path.
     *
     * @param httpMethod HTTP method type.
     * @param path       The request path.
     * @return Matched method or null if not found.
     */
    public static Method findRouteHandler(String httpMethod, String path) {
        for (String key : routeHandlers.keySet()) {
            if (key.startsWith(httpMethod)) {
                String pattern = key.split(":", 2)[1];
                if (Pattern.matches(pattern, path)) {
                    return routeHandlers.get(key);
                }
            }
        }
        return null;
    }

    /**
     * Retrieves the original route pattern for a given key.
     */
    public static String getOriginalRoute(String httpMethod, String path) {
        for (String key : routePatterns.keySet()) {
            if (key.startsWith(httpMethod)) {
                String pattern = key.split(":", 2)[1];
                if (Pattern.matches(pattern, path)) {
                    return routePatterns.get(key);
                }
            }
        }
        return null;
    }
}
