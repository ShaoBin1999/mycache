package com.bsren.mycache.annoation;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface CacheEvict {

    @AliasFor("cacheNames")
    String[] value() default {};

    @AliasFor("value")
    String[] cacheNames() default {};

    String key() default "";

    String keyGenerator() default "";

    String cacheManager() default "";

    String cacheResolver() default "";

    String condition() default "";

    /**
     * Whether all the entries inside the cache(s) are removed.
     * <p>By default, only the value under the associated key is removed.
     * <p>Note that setting this parameter to {@code true} and specifying a
     * {@link #key} is not allowed.
     */
    boolean allEntries() default false;

    /**
     * Whether the eviction should occur before the method is invoked.
     * <p>Setting this attribute to {@code true}, causes the eviction to
     * occur irrespective of the method outcome (i.e., whether it threw an
     * exception or not).
     * <p>Defaults to {@code false}, meaning that the cache eviction operation
     * will occur <em>after</em> the advised method is invoked successfully (i.e.
     * only if the invocation did not throw an exception).
     */
    boolean beforeInvocation() default false;
}
