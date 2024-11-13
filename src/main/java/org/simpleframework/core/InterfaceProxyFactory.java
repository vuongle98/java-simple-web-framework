package org.simpleframework.core;

import java.lang.reflect.Proxy;

public class InterfaceProxyFactory {

    public static Object createProxy(Class<?> interfaceClass) {
        return Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[] {interfaceClass},
                (proxy, method, args) -> method.invoke(null, args)
        );
    }
}
