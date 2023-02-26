package com.bsren.mycache.interceptor;

import org.springframework.lang.Nullable;

import java.lang.reflect.Method;
import java.util.Collection;

public interface CacheOperationSource {

    default boolean isCandidateClass(Class<?> targetClass) {
        return true;
    }

    @Nullable
    Collection<CacheOperation> getCacheOperations(Method method, @Nullable Class<?> targetClass);

}
