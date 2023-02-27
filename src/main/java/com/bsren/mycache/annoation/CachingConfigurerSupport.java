package com.bsren.mycache.annoation;

import com.bsren.mycache.CacheManager;
import com.bsren.mycache.interceptor.CacheErrorHandler;
import com.bsren.mycache.interceptor.CacheResolver;
import com.bsren.mycache.interceptor.KeyGenerator;
import org.springframework.lang.Nullable;

public class CachingConfigurerSupport implements CachingConfigurer {

    @Override
    @Nullable
    public CacheManager cacheManager() {
        return null;
    }

    @Override
    @Nullable
    public CacheResolver cacheResolver() {
        return null;
    }

    @Override
    @Nullable
    public KeyGenerator keyGenerator() {
        return null;
    }

    @Override
    @Nullable
    public CacheErrorHandler errorHandler() {
        return null;
    }

}

