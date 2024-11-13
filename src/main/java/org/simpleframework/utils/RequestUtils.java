package org.simpleframework.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.httpserver.core.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class RequestUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Parses the request body as JSON into an instance of the given parameter type.
     *
     * @param exchange  The HTTP exchange object.
     * @param parameter The method parameter to deserialize into.
     * @return An instance of the parameter type populated with request data.
     */
    public static Object parseRequestBody(HttpExchange exchange, Parameter parameter) {
        try (InputStream is = exchange.getRequestBody()) {
            return objectMapper.readValue(is, parameter.getType());
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse request body: " + e.getMessage());
        }
    }

    /**
     * Converts a dynamic path to a regex pattern, e.g., /user/{id} -> /user/(?<id>\\w+)
     * and converts a dynamic query to a regex pattern, e.g., ?username=abc
     *
     * @param path the full path request.
     */
    public static String convertToRegex(String path) {
        String pathPattern = path.replaceAll("\\{(\\w+)}", "(?<$1>\\\\w+)");
        String queryPattern = "(\\?.*)?";

        return "^" + pathPattern + queryPattern + "$";
    }

    /**
     * Converts a string value to the specified type (int, long, String, etc.).
     */
    public static Object convertToType(String value, Class<?> type) {
        if (type == Integer.class || type == int.class) {
            return Integer.parseInt(value);
        } else if (type == Long.class || type == long.class) {
            return Long.parseLong(value);
        } else if (type == Double.class || type == double.class) {
            return Double.parseDouble(value);
        } else if (type == Boolean.class || type == boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (type == Float.class || type == float.class) {
            return Float.parseFloat(value);
        } else if (type == Short.class || type == short.class) {
            return Short.parseShort(value);
        } else if (type == Byte.class || type == byte.class) {
            return Byte.parseByte(value);
        } else if (type == String.class) {
            return String.valueOf(value);
        }
        return value; // Default to String if type is unknown
    }

    public static Object parseRequestBody(HttpExchange exchange, Class<?> type) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return objectMapper.readValue(is, type);
        }
    }

    public static Map<String, String> parseQueryParams(String query) {
        Map<String, String> queryParams = new HashMap<>();

        if (query == null || query.isEmpty()) {
            return queryParams;
        }

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");

//            if (keyValue.length != 2) {
//                throw new IllegalArgumentException("Invalid query parameter: " + pair);
//            }

            String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
            String value = keyValue.length > 1 ? URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8) : "";
            queryParams.put(key, value);
        }

        return queryParams;
    }
}
