package org.simpleframework.utils;

import org.simpleframework.annotations.PathVariable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathUtils {

    public static Object[] extractPathVar(Method handlerMethod, String path, String routePattern) {
        String regexPattern = routePattern.replaceAll("\\{(\\w+)}", "(?<$1>\\w+)");
        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(path);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid path: " + path);
        }

        Parameter[] parameters = handlerMethod.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(PathVariable.class)) {
                String paramName = parameters[i].getAnnotation(PathVariable.class).value();
                String match = matcher.group(paramName);
                args[i] = convertToType(match, parameters[i].getType());
            }
        }

        return args;
    }

    private static Object convertToType(String value, Class<?> type) {
        if (type == int.class || type == Integer.class) {
            return Integer.parseInt(value);
        }
        if (type == long.class || type == Long.class) {
            return Long.parseLong(value);
        }
        if (type == double.class || type == Double.class) {
            return Double.parseDouble(value);
        }
        return value;
    }

}
