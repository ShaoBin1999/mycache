package com.bsren.mycache.annoation;

import com.bsren.mycache.interceptor.CacheOperation;
import org.springframework.cache.annotation.AnnotationCacheOperationSource;
import org.springframework.lang.Nullable;

import java.lang.reflect.Method;
import java.util.Collection;

public interface CacheAnnotationParser {

    /**
     * Determine whether the given class is a candidate for cache operations
     * in the annotation format of this {@code CacheAnnotationParser}.
     * <p>If this method returns {@code false}, the methods on the given class
     * will not get traversed for {@code #parseCacheAnnotations} introspection.
     * Returning {@code false} is therefore an optimization for non-affected
     * classes, whereas {@code true} simply means that the class needs to get
     * fully introspected for each method on the given class individually.
     * @param targetClass the class to introspect
     * @return {@code false} if the class is known to have no cache operation
     * annotations at class or method level; {@code true} otherwise. The default
     * implementation returns {@code true}, leading to regular introspection.
     * @since 5.2
     */
    default boolean isCandidateClass(Class<?> targetClass) {
        return true;
    }

    @Nullable
    Collection<CacheOperation> parseCacheAnnotations(Class<?> type);


    @Nullable
    Collection<CacheOperation> parseCacheAnnotations(Method method);
}
