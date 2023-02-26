package com.bsren.mycache.interceptor;

import java.lang.reflect.Method;

public interface CacheOperationInvocationContext<O extends BasicOperation> {

    O getOperation();

    /**
     * Return the target instance on which the method was invoked.
     */
    Object getTarget();

    /**
     * Return the method which was invoked.
     */
    Method getMethod();

    /**
     * Return the argument list used to invoke the method.
     */
    Object[] getArgs();
}
