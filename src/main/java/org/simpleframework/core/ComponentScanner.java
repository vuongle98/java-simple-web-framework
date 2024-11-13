package org.simpleframework.core;

import org.simpleframework.annotations.*;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class ComponentScanner {

    public static final List<Class<? extends Annotation>> COMPONENT_ANNOTATIONS = List.of(
            Controller.class,
            Component.class,
            Service.class
    );

    public static void scanAndRegisterComponents(Class<?> applicationClass) {
        ComponentScan componentScan = applicationClass.getAnnotation(ComponentScan.class);

        if (componentScan == null) {
            throw new RuntimeException("No @ComponentScan annotation found");
        }

        for (String basePackage : componentScan.basePackages()) {
            try {
                List<Class<?>> componentClasses = findAnnotatedClasses(basePackage, COMPONENT_ANNOTATIONS);

                for (Class<?> cls : componentClasses) {
                    Injector.register(cls, null);
                    Dispatcher.register(cls);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private static List<Class<?>> findAnnotatedClasses(String basePackage, List<Class<? extends Annotation>> componentAnnotations) throws IOException, ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        String packagePath = basePackage.replace('.', '/');

        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(packagePath);

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            if (resource.getProtocol().equals("file")) {
                classes.addAll(findClassesInDirectory(new File(resource.getFile()), basePackage));
            } else if (resource.getProtocol().equals("jar")) {
                String jarPath = resource.getPath().substring(5, resource.getPath().indexOf("!"));
                classes.addAll(findClassesInJar(jarPath, packagePath));
            }
        }

        return classes.stream()
                .filter(cls -> componentAnnotations.stream().anyMatch(cls::isAnnotationPresent))
                .collect(Collectors.toList());
    }

    private static List<Class<?>> findClassesInDirectory(File directory, String basePackage) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();

        if (!directory.exists()) return classes;

        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (file.isDirectory()) {
                classes.addAll(findClassesInDirectory(file, basePackage + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                String className = basePackage + "." + file.getName().substring(0, file.getName().length() - 6);
                Class<?> cls = Class.forName(className);
//                if (!Modifier.isAbstract(cls.getModifiers())) {
                    classes.add(cls);
//                }
            }
        }
        return classes;
    }

    private static List<Class<?>> findClassesInJar(String jarPath, String packageName) throws IOException, ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();

        try (JarFile jarFile = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();

                if (entryName.startsWith(packageName) && entryName.endsWith(".class")) {
                    String className = entryName.replace('/', '.').substring(0, entryName.length() - 6);
                    Class<?> cls = Class.forName(className);

                    if (!Modifier.isAbstract(cls.getModifiers())) {
                        classes.add(cls);
                    }
                }
            }
        }
        return classes;
    }

}
