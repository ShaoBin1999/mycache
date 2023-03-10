package com.bsren.mycache.interceptor;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.MethodClassKey;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractFallbackCacheOperationSource  implements CacheOperationSource {

    private static final Collection<CacheOperation> NULL_CACHING_ATTRIBUTE = Collections.emptyList();

    protected final Log logger = LogFactory.getLog(getClass());

    /**
     * Cache of CacheOperations, keyed by method on a specific target class.
     * <p>As this base class is not marked Serializable, the cache will be recreated
     * after serialization - provided that the concrete subclass is Serializable.
     */
    private final Map<Object, Collection<CacheOperation>> attributeCache = new ConcurrentHashMap<>(1024);

    @Override
    @Nullable
    public Collection<CacheOperation> getCacheOperations(Method method, @Nullable Class<?> targetClass) {
        if (method.getDeclaringClass() == Object.class) {
            return null;
        }

        Object cacheKey = getCacheKey(method, targetClass);
        Collection<CacheOperation> cached = this.attributeCache.get(cacheKey);

        if (cached != null) {
            return (cached != NULL_CACHING_ATTRIBUTE ? cached : null);
        }
        else {
            Collection<CacheOperation> cacheOps = computeCacheOperations(method, targetClass);
            if (cacheOps != null) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Adding cacheable method '" + method.getName() + "' with attribute: " + cacheOps);
                }
                this.attributeCache.put(cacheKey, cacheOps);
            }
            else {
                this.attributeCache.put(cacheKey, NULL_CACHING_ATTRIBUTE);
            }
            return cacheOps;
        }
    }

    /**
     * Determine a cache key for the given method and target class.
     * <p>Must not produce same key for overloaded methods.
     * Must produce same key for different instances of the same method.
     * @param method the method (never {@code null})
     * @param targetClass the target class (may be {@code null})
     * @return the cache key (never {@code null})
     */
    protected Object getCacheKey(Method method, @Nullable Class<?> targetClass) {
        return new MethodClassKey(method, targetClass);
    }

    @Nullable
    private Collection<CacheOperation> computeCacheOperations(Method method, @Nullable Class<?> targetClass) {
        // Don't allow non-public methods, as configured.
        if (allowPublicMethodsOnly() && !Modifier.isPublic(method.getModifiers())) {
            return null;
        }

        // The method may be on an interface, but we need attributes from the target class.
        // If the target class is null, the method will be unchanged.
        Method specificMethod = AopUtils.getMostSpecificMethod(method, targetClass);

        // First try is the method in the target class.
        Collection<CacheOperation> opDef = findCacheOperations(specificMethod);
        if (opDef != null) {
            return opDef;
        }

        // Second try is the caching operation on the target class.
        opDef = findCacheOperations(specificMethod.getDeclaringClass());
        if (opDef != null && ClassUtils.isUserLevelMethod(method)) {
            return opDef;
        }

        if (specificMethod != method) {
            // Fallback is to look at the original method.
            opDef = findCacheOperations(method);
            if (opDef != null) {
                return opDef;
            }
            // Last fallback is the class of the original method.
            opDef = findCacheOperations(method.getDeclaringClass());
            if (opDef != null && ClassUtils.isUserLevelMethod(method)) {
                return opDef;
            }
        }

        return null;
    }


    /**
     * Subclasses need to implement this to return the caching attribute for the
     * given class, if any.
     * @param clazz the class to retrieve the attribute for
     * @return all caching attribute associated with this class, or {@code null} if none
     */
    @Nullable
    protected abstract Collection<CacheOperation> findCacheOperations(Class<?> clazz);

    /**
     * Subclasses need to implement this to return the caching attribute for the
     * given method, if any.
     * @param method the method to retrieve the attribute for
     * @return all caching attribute associated with this method, or {@code null} if none
     */
    @Nullable
    protected abstract Collection<CacheOperation> findCacheOperations(Method method);

    /**
     * Should only public methods be allowed to have caching semantics?
     * <p>The default implementation returns {@code false}.
     */
    protected boolean allowPublicMethodsOnly() {
        return false;
    }

}
