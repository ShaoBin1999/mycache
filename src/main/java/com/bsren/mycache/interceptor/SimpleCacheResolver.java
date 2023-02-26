package com.bsren.mycache.interceptor;

import com.bsren.mycache.CacheManager;
import org.springframework.lang.Nullable;

import java.util.Collection;

public class SimpleCacheResolver extends AbstractCacheResolver{

    public SimpleCacheResolver() {
    }


    public SimpleCacheResolver(CacheManager cacheManager) {
        super(cacheManager);
    }


    @Override
    protected Collection<String> getCacheNames(CacheOperationInvocationContext<?> context) {
        return context.getOperation().getCacheNames();
    }


    /**
     * Return a {@code SimpleCacheResolver} for the given {@link CacheManager}.
     */
    @Nullable
    static SimpleCacheResolver of(@Nullable CacheManager cacheManager) {
        return (cacheManager != null ? new SimpleCacheResolver(cacheManager) : null);
    }

}
