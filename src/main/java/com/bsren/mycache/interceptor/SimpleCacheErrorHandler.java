package com.bsren.mycache.interceptor;

import com.bsren.mycache.Cache;

public class SimpleCacheErrorHandler implements CacheErrorHandler{

    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        throw exception;
    }

    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
        throw exception;
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        throw exception;
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        throw exception;
    }
}
