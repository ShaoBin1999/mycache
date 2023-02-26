package com.bsren.mycache.interceptor;

import com.bsren.mycache.Cache;
import org.springframework.lang.Nullable;
import org.springframework.util.function.SingletonSupplier;

public abstract class AbstractCacheInvoker {

    protected SingletonSupplier<CacheErrorHandler> errorHandler;

    protected AbstractCacheInvoker() {
        this.errorHandler = SingletonSupplier.of(SimpleCacheErrorHandler::new);
    }

    protected AbstractCacheInvoker(CacheErrorHandler errorHandler) {
        this.errorHandler = SingletonSupplier.of(errorHandler);
    }

    public void setErrorHandler(CacheErrorHandler errorHandler) {
        this.errorHandler = SingletonSupplier.of(errorHandler);
    }

    /**
     * Return the {@link CacheErrorHandler} to use.
     */
    public CacheErrorHandler getErrorHandler() {
        return this.errorHandler.obtain();
    }

    /**
     * Execute {@link Cache#get(Object)} on the specified {@link Cache} and
     * invoke the error handler if an exception occurs. Return {@code null}
     * if the handler does not throw any exception, which simulates a cache
     * miss in case of error.
     */
    @Nullable
    protected Cache.ValueWrapper doGet(Cache cache, Object key) {
        try {
            return cache.get(key);
        }
        catch (RuntimeException ex) {
            getErrorHandler().handleCacheGetError(ex, cache, key);
            return null;  // If the exception is handled, return a cache miss
        }
    }

    protected void doPut(Cache cache, Object key, @Nullable Object result) {
        try {
            cache.put(key, result);
        }
        catch (RuntimeException ex) {
            getErrorHandler().handleCachePutError(ex, cache, key, result);
        }
    }

    protected void doEvict(Cache cache, Object key, boolean immediate) {
        try {
            if (immediate) {
                cache.evictIfPresent(key);
            }
            else {
                cache.evict(key);
            }
        }
        catch (RuntimeException ex) {
            getErrorHandler().handleCacheEvictError(ex, cache, key);
        }
    }

    protected void doClear(Cache cache, boolean immediate) {
        try {
            if (immediate) {
                cache.invalidate();
            }
            else {
                cache.clear();
            }
        }
        catch (RuntimeException ex) {
            getErrorHandler().handleCacheClearError(ex, cache);
        }
    }
}
