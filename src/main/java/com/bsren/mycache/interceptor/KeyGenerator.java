package com.bsren.mycache.interceptor;

import java.lang.reflect.Method;

@FunctionalInterface
public interface KeyGenerator{

    Object generate(Object target, Method method,Object... params);
}
