package com.amos.crpc.proxy;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Mock服务代理
 */
@Slf4j
public class MockServiceProxy implements InvocationHandler {
    /**
     * 调用代理
     *
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 根据方法返回值类型，返回特定的默认值对象
        Class<?> methodReturnType = method.getReturnType();
        log.info("mock invoke method: {}", method.getName());
        return getDefaultObject(methodReturnType);
    }

    /**
     * 根据方法返回值类型，返回特定的默认值对象
     *
     * @param methodReturnType
     * @return
     */
    private Object getDefaultObject(Class<?> methodReturnType) {
        // 基本数据类型
        if (methodReturnType.isPrimitive()) {
            if (methodReturnType == short.class) {
                return (short) 0;
            } else if (methodReturnType == byte.class || methodReturnType == int.class) {
                return 0;
            } else if (methodReturnType == long.class) {
                return 0L;
            } else if (methodReturnType == float.class) {
                return 0.0F;
            } else if (methodReturnType == double.class) {
                return 0.0D;
            } else if (methodReturnType == char.class) {
                return '0';
            } else if (methodReturnType == boolean.class) {
                return false;
            }
        }
        // 引用数据类型
        return null;
    }
}
