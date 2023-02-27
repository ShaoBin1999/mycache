package com.bsren.mycache.annoation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheConfig {

    String[] cacheNames() default {};

    String keyGenerator() default "";

    String cacheManager() default "";

    String cacheResolver() default "";

}
