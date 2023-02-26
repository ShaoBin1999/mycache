package com.bsren.mycache.interceptor;

import com.bsren.mycache.Cache;
import org.springframework.lang.Nullable;

public interface CacheErrorHandler {

    /**
     * Handle the given runtime exception thrown by the cache provider when
     * retrieving an item with the specified {@code key}, possibly
     * rethrowing it as a fatal exception.
     */
    void handleCacheGetError(RuntimeException exception, Cache cache, Object key);

    /**
     * Handle the given runtime exception thrown by the cache provider when
     * updating an item with the specified {@code key} and {@code value},
     * possibly rethrowing it as a fatal exception.
     */
    void handleCachePutError(RuntimeException exception, Cache cache, Object key, @Nullable Object value);

    /**
     * Handle the given runtime exception thrown by the cache provider when
     * clearing an item with the specified {@code key}, possibly rethrowing
     * it as a fatal exception.
     */
    void handleCacheEvictError(RuntimeException exception, Cache cache, Object key);

    /**
     * Handle the given runtime exception thrown by the cache provider when
     * clearing the specified {@link Cache}, possibly rethrowing it as a
     * fatal exception.
     */
    void handleCacheClearError(RuntimeException exception, Cache cache);
}
