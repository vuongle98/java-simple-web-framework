package org.simpleframework.core;

import org.simpleframework.database.JpaRepositoryImpl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public class RepositoryInvocationHandler<T> implements InvocationHandler {

    private final JpaRepositoryImpl<T, ?> repositoryImpl;

    public RepositoryInvocationHandler(JpaRepositoryImpl<T, ?> repositoryImpl) {
        this.repositoryImpl = repositoryImpl;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Delegate method invocation to the JpaRepositoryImpl instance
        return method.invoke(repositoryImpl, args);
    }


    private Class<?> extractEntityType(Class<?> repositoryInterface) {
        // Extract the entity type from JpaRepository<User, Long>
        Type[] genericInterfaces = repositoryInterface.getGenericInterfaces();
        for (Type type : genericInterfaces) {
            if (type instanceof ParameterizedType parameterizedType) {
                return (Class<?>) parameterizedType.getActualTypeArguments()[0];
            }
        }
        return Object.class; // Fallback if not found
    }

}



