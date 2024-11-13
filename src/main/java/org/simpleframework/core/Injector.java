package org.simpleframework.core;

import org.simpleframework.annotations.Autowired;

import java.lang.reflect.*;
import java.util.*;


public class Injector {

    private static final Map<Class<?>, Object> registry = new HashMap<>();

    public static <T> void register(Class<T> cls, Class<?> entityType) throws Exception {

        if (cls.isInterface()) {
            registerRepository(cls, entityType);
        } else {
            Object instance = createInstance(cls);
            injectDependencies(cls, instance);
            registry.put(cls, instance);
        }
    }

    public static <T> void registerRepository(Class<T> cls, Class<?> entityType) throws Exception {
        if (cls.isInterface()) {

            Object proxyInstance = RepositoryFactory.createRepository(cls, entityType);
            injectDependencies(proxyInstance.getClass(), proxyInstance);
            registry.put(cls, proxyInstance);
        }
    }

    public static <T> T get(Class<T> cls, Class<?> entityType) {
        if (!registry.containsKey(cls)) {
            try {
                register(cls, entityType);
            } catch (Exception e) {
                throw new RuntimeException("Failed to register class " + cls.getName(), e);
            }
        }
        return cls.cast(registry.get(cls));
    }

    private static Object createInstance(Class<?> clazz) throws Exception {
        System.out.println("Initial service " + clazz.getName());

        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (constructor.isAnnotationPresent(Autowired.class)) {
                Class<?>[] paramTypes = constructor.getParameterTypes();
//                Type[] types = constructor.getGenericParameterTypes();

                Class<?> entityType = getEntityType(clazz);

                Object[] params = Arrays.stream(paramTypes)
                        .map(paramType -> get(paramType, entityType))
                        .toArray();
                return constructor.newInstance(params);
            }
        }
        return clazz.getDeclaredConstructor().newInstance();
    }

    private static <T> void injectDependencies(Class<T> cls, Object instance) throws IllegalAccessException {
        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Autowired.class)) {
                Class<?> entityType = getEntityType(cls);
                Object dependency = get(field.getType(), entityType);
                if (dependency == null) {
                    throw new RuntimeException("No instance found for type " + field.getType());
                }

                field.setAccessible(true);
                field.set(instance, dependency);
            }
        }
    }

    private static Class<?> getEntityType(Class<?> cls) {
        Constructor<?>[] constructors = cls.getDeclaredConstructors();

        for (Constructor<?> constructor : constructors) {
            Type[] types = constructor.getGenericParameterTypes();

            for (Type type : types) {
                if (type instanceof ParameterizedType) {
                    Type entityType = ((ParameterizedType) type).getActualTypeArguments()[0];

                    if (entityType instanceof Class<?>) {
                        System.out.println(((Class<?>) entityType).getName());
                        return (Class<?>) entityType;
                    }
                }
            }
        }
        return null;
    }

    // Method to find implementation of an interface (for simplicity, this is a basic lookup)
    private static Class<?> findImplementation(Class<?> interfaceClass) {
        // Look for a concrete class that implements the interface
        for (Class<?> clazz : registry.keySet()) {
            if (interfaceClass.isAssignableFrom(clazz) && !clazz.isInterface()) {
                return clazz;  // Return the first concrete class that implements the interface
            }
        }
        return null;  // No implementation found
    }


    private static Object createInterfaceProxy(Class<?> cls, Object implInstance) {
        return Proxy.newProxyInstance(
                cls.getClassLoader(),
                new Class[]{cls},
                (proxy, method, args) -> method.invoke(implInstance, args)
        );
    }
}
