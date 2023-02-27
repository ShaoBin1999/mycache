package com.bsren.mycache.annoation;

import com.bsren.mycache.CacheManager;
import com.bsren.mycache.interceptor.CacheErrorHandler;
import com.bsren.mycache.interceptor.CacheResolver;
import com.bsren.mycache.interceptor.KeyGenerator;
import org.springframework.lang.Nullable;

/**
 * Interface to be implemented by @Configuration classes annotated with @EnableCaching
 * that wish or need to specify explicitly how caches are resolved
 * and how keys are generated for annotation-driven cache management
 *
 * 配置类
 */
public interface CachingConfigurer {

    @Nullable
    default CacheManager cacheManager() {
        return null;
    }


    @Nullable
    default CacheResolver cacheResolver() {
        return null;
    }

    @Nullable
    default KeyGenerator keyGenerator() {
        return null;
    }

    @Nullable
    default CacheErrorHandler errorHandler() {
        return null;
    }
}
