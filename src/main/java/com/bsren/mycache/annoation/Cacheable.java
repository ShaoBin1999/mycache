package com.bsren.mycache.annoation;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Cacheable {

    @AliasFor("cacheNames")
    String[] value() default {};

    @AliasFor("value")
    String[] cacheNames() default {};

    String key() default "";

    String keyGenerator() default "";

    String cacheManager() default "";

    String cacheResolver() default "";

    String condition() default "";

    String unless() default "";

    /**
     * Synchronize the invocation of the underlying method if several threads are attempting to load a value for the same key.
     * The synchronization leads to a couple of limitations:
     * unless() is not supported
     * Only one cache may be specified
     * No other cache-related operation can be combined
     * This is effectively a hint and the actual cache provider that you are using may not support it in a synchronized fashion.
     * Check your provider documentation for more details on the actual semantics.
     */
    boolean sync() default false;
}
