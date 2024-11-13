package org.simpleframework.core;

import org.httpserver.core.HttpExchange;
import org.simpleframework.annotations.*;
import org.simpleframework.utils.RequestUtils;
import org.simpleframework.utils.ResponseUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Dispatcher {

    public static void register(Class<?> cls) {

        for (Method method : cls.getDeclaredMethods()) {

            if (method.isAnnotationPresent(GetMapping.class)) {
                GetMapping getMapping = method.getAnnotation(GetMapping.class);
                RouteRegistry.registerRoute("GET", getMapping.value(), method);
            }
            if (method.isAnnotationPresent(PostMapping.class)) {
                PostMapping postMapping = method.getAnnotation(PostMapping.class);
                RouteRegistry.registerRoute("POST", postMapping.value(), method);
            }
            // Add other HTTP methods (PUT, DELETE, etc.) as needed
            if (method.isAnnotationPresent(PutMapping.class)) {
                PutMapping putMapping = method.getAnnotation(PutMapping.class);
                String path = putMapping.value();
                RouteRegistry.registerRoute("PUT", path, method);
            }

            if (method.isAnnotationPresent(DeleteMapping.class)) {
                DeleteMapping deleteMapping = method.getAnnotation(DeleteMapping.class);
                String path = deleteMapping.value();
                RouteRegistry.registerRoute("DELETE", path, method);
            }
        }
    }

    public static void handle(HttpExchange exchange) {
        String path = exchange.getPath();
        String method = exchange.getMethod();
        Map<String, String> queryParams = exchange.getQueryParams();

        Method handler = RouteRegistry.findRouteHandler(method, path);

        if (handler == null) {
            ResponseUtils.sendResponse(exchange, 404, "Not Found");
            return;
        }

        try {
            Object controller = Injector.get(handler.getDeclaringClass(), null);
            String originalPattern = RouteRegistry.getOriginalRoute(method, path);

            Object[] args = buildMethodArgs(handler, exchange, originalPattern, path, queryParams);

            Object response = handler.invoke(controller, args);
            ResponseUtils.sendResponse(exchange, 200, response != null ? response.toString() : "");

        } catch (IOException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            ResponseUtils.sendResponse(exchange, 500, "Internal Server Error");
        }
    }

    private static Object[] buildMethodArgs(Method method, HttpExchange exchange, String routePattern, String path, Map<String, String> queryParams) throws IOException {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        Pattern pattern = Pattern.compile(RequestUtils.convertToRegex(routePattern));
        Matcher matcher = pattern.matcher(path);

        if (matcher.matches()) {
            for (int i = 0; i < parameters.length; i++) {
                Parameter param = parameters[i];
                args[i] = resolveParameter(param, exchange, matcher, queryParams);
            }
        }

        return args;
    }

    private static Object resolveParameter(Parameter param, HttpExchange exchange, Matcher matcher, Map<String, String> queryParams) throws IOException {
        if (param.isAnnotationPresent(PathVariable.class)) {
            return handlePathVariable(param, matcher);
        } else if (param.isAnnotationPresent(RequestBody.class)) {
            return handleRequestBody(param, exchange);
        } else if (param.isAnnotationPresent(RequestParam.class)) {
            return handleRequestParam(param, queryParams);
        }
        return null;
    }

    private static Object handlePathVariable(Parameter param, Matcher matcher) {
        String varName = param.getAnnotation(PathVariable.class).value();
        String value = matcher.group(varName);
        return RequestUtils.convertToType(value, param.getType());
    }

    private static Object handleRequestBody(Parameter param, HttpExchange exchange) throws IOException {
        return RequestUtils.parseRequestBody(exchange, param.getType());
    }

    private static Object handleRequestParam(Parameter param, Map<String, String> queryParams) {
        RequestParam requestParam = param.getAnnotation(RequestParam.class);
        String value = queryParams.get(requestParam.value());
        if (value == null && requestParam.required()) {
            throw new IllegalArgumentException("Missing required parameter " + requestParam.value());
        }
        return RequestUtils.convertToType(value, param.getType());
    }

}
