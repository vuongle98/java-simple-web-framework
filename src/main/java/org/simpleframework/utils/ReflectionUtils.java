package org.simpleframework.utils;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.stream.Collectors;

public class ReflectionUtils {

    /**
     * Scans the specified package for classes annotated with any of the provided annotations.
     *
     * @param packageName The base package to scan.
     * @param annotations The list of annotations to search for.
     * @return A set of classes that are annotated with one of the specified annotations.
     */
    public static Set<Class<?>> getAnnotatedClasses(String packageName, Class<? extends Annotation>... annotations) {

        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .forPackage(packageName)
                        .addScanners(Scanners.TypesAnnotated)
        );

        return reflections.getTypesAnnotatedWith(annotations[0])
                .stream()
                .filter(cls -> {
                    for (Class<? extends Annotation> annotation : annotations) {
                        if (cls.isAnnotationPresent(annotation)) {
                            return true;
                        }
                    }
                    return false;
                }).collect(Collectors.toSet());
    }

    public static Set<Class<?>> getAnnotatedClasses(Class<? extends Annotation> annotationClass) {
        Reflections reflections = new Reflections("org.vuong");
        return reflections.getTypesAnnotatedWith(annotationClass);
    }
}
