package org.simpleframework.core;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.simpleframework.database.JpaRepository;
import org.simpleframework.database.JpaRepositoryImpl;
import org.simpleframework.models.User;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RepositoryFactory {

    private static final Map<String, Class<?>> typeMap = Map.of(
            "T", User.class, // This maps the TypeVariable "T" to the concrete User class
            "ID", Integer.class
    );

    private static final Map<Class<?>, Object> repositoryCache = new HashMap<>();

    public static <T, ID> JpaRepository<T, ID> createRepositoryInstance(Class<?> repositoryInterface) {
        Type[] genericInterfaces = repositoryInterface.getGenericInterfaces();
        if (genericInterfaces.length > 0) {
            Type genericType = genericInterfaces[0];
            if (genericType instanceof ParameterizedType parameterizedType) {

                // Extract entityType and idType from the generic parameters
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (actualTypeArguments.length == 2) {

                    Type entityType = actualTypeArguments[0];

                    if (entityType instanceof TypeVariable) {
                        // Look up the actual type in the type map or use default logic
                        entityType = resolveType((TypeVariable<?>) entityType);
                    }

                    if (entityType instanceof Class<?>) {
                        @SuppressWarnings("unchecked")
                        Class<T> entityClass = (Class<T>) entityType;

                        // Create an instance of JpaRepositoryImpl using the entity and ID types
                        return new JpaRepositoryImpl<>(entityClass);
                    }
                    // Create an instance of JpaRepositoryImpl using the entity type

                }
            }
        }
        throw new IllegalArgumentException("Unable to extract entity types from JpaRepository");
    }

    private static Type resolveType(TypeVariable<?> typeVariable) {
        String typeName = typeVariable.getName();

        // Look up the resolved class from the type map, or throw an exception
        if (typeMap.containsKey(typeName)) {
            return typeMap.get(typeName);
        }

        throw new IllegalArgumentException("Unable to resolve TypeVariable: " + typeName);
    }

    public static Object createRepository(Class<?> repositoryInterface) {
        if (repositoryCache.containsKey(repositoryInterface)) {
            return repositoryCache.get(repositoryInterface);
        }

        try {
            // Find the implementation class dynamically
            String implClassName = repositoryInterface.getName() + "Impl";
            Class<?> implClass = Class.forName(implClassName);
            Object implInstance = implClass.getDeclaredConstructor().newInstance();

            Object proxyInstance = Proxy.newProxyInstance(
                    repositoryInterface.getClassLoader(),
                    new Class[]{repositoryInterface},
                    (proxy, method, args) -> method.invoke(implInstance, args)
            );

            repositoryCache.put(repositoryInterface, proxyInstance);
            return proxyInstance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create repository proxy", e);
        }
    }

    public static <T> Object createRepository(Class<T> repositoryInterface, Class<?> entityType) {
        if (repositoryCache.containsKey(repositoryInterface)) {
            return repositoryCache.get(repositoryInterface);
        }

        try {
            // Find the implementation class dynamically
            Set<Class<? extends T>> foundImplements = RepositoryFactory.findImplementations(repositoryInterface, "org.simpleframework");

            if (foundImplements.size() > 1) {
                throw new RuntimeException("Existed more than 1 implement for interface " + repositoryInterface.getName());
            }

            for (Class<? extends T> implClass : foundImplements) {
                Object implInstance = implClass.getDeclaredConstructors()[0].newInstance(entityType);

                Object proxyInstance = Proxy.newProxyInstance(
                        repositoryInterface.getClassLoader(),
                        new Class[]{repositoryInterface},
                        (proxy, method, args) -> method.invoke(implInstance, args)
                );

                repositoryCache.put(repositoryInterface, proxyInstance);
                return proxyInstance;
            }

            throw new RuntimeException("Failed to create repository proxy");

//            String implClassName = repositoryInterface.getName() + "Impl";
//            Class<?> implClass = Class.forName(implClassName);
//            Object implInstance = implClass.getDeclaredConstructors()[0].newInstance(entityType);
//
//            Object proxyInstance = Proxy.newProxyInstance(
//                    repositoryInterface.getClassLoader(),
//                    new Class[]{repositoryInterface},
//                    (proxy, method, args) -> method.invoke(implInstance, args)
//            );
//
//            repositoryCache.put(repositoryInterface, proxyInstance);
//            return proxyInstance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create repository proxy", e);
        }
    }

    public static <T> Set<Class<? extends T>> findImplementations(Class<T> interfaceClass, String packageName) {
        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .forPackage(packageName)
                        .addScanners(Scanners.SubTypes)
        );
        return reflections.getSubTypesOf(interfaceClass);
    }

    private static Class<?> extractEntityType(Class<?> repositoryInterface) {
        try {

            // Extract the first generic type argument (which is the entity class) from the JpaRepository interface
            ParameterizedType genericSuperclass = (ParameterizedType) repositoryInterface.getGenericInterfaces()[0];
            Type type = genericSuperclass.getActualTypeArguments()[0];
            return (Class<?>) genericSuperclass.getActualTypeArguments()[0];
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
