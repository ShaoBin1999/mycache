package com.bsren.mycache.interceptor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.lang.reflect.Method;

public class CacheInterceptor extends CacheAspectSupport implements MethodInterceptor, Serializable {

    @Override
    @Nullable
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();

        CacheOperationInvoker aopAllianceInvoker = () -> {
            try {
                return invocation.proceed();
            }
            catch (Throwable ex) {
                throw new CacheOperationInvoker.ThrowableWrapper(ex);
            }
        };

        Object target = invocation.getThis();
        Assert.state(target != null, "Target must not be null");
        try {
            return execute(aopAllianceInvoker, target, method, invocation.getArguments());
        }
        catch (CacheOperationInvoker.ThrowableWrapper th) {
            throw th.getOriginal();
        }
    }
}
