package com.bsren.mycache.interceptor;

import com.bsren.mycache.CacheManager;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.lang.reflect.Method;

abstract class CacheOperationSourcePointcut extends StaticMethodMatcherPointcut implements Serializable {

    protected CacheOperationSourcePointcut() {
        setClassFilter(new CacheOperationSourceClassFilter());
    }


    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        CacheOperationSource cas = getCacheOperationSource();
        return (cas != null && !CollectionUtils.isEmpty(cas.getCacheOperations(method, targetClass)));
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CacheOperationSourcePointcut)) {
            return false;
        }
        CacheOperationSourcePointcut otherPc = (CacheOperationSourcePointcut) other;
        return ObjectUtils.nullSafeEquals(getCacheOperationSource(), otherPc.getCacheOperationSource());
    }

    @Override
    public int hashCode() {
        return CacheOperationSourcePointcut.class.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getName() + ": " + getCacheOperationSource();
    }


    /**
     * Obtain the underlying {@link org.springframework.cache.interceptor.CacheOperationSource} (may be {@code null}).
     * To be implemented by subclasses.
     */
    @Nullable
    protected abstract CacheOperationSource getCacheOperationSource();

    private class CacheOperationSourceClassFilter implements ClassFilter {

        @Override
        public boolean matches(Class<?> clazz) {
            if (CacheManager.class.isAssignableFrom(clazz)) {
                return false;
            }
            CacheOperationSource cas = getCacheOperationSource();
            return (cas == null || cas.isCandidateClass(clazz));
        }
    }

}
