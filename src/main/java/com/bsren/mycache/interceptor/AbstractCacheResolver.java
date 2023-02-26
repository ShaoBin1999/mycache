package com.bsren.mycache.interceptor;

import com.bsren.mycache.Cache;
import com.bsren.mycache.CacheManager;
import com.bsren.mycache.support.AbstractCacheManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public abstract class AbstractCacheResolver implements CacheResolver, InitializingBean {

    private CacheManager cacheManager;

    protected AbstractCacheResolver(){}

    protected AbstractCacheResolver(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public CacheManager getCacheManager() {
        Assert.state(this.cacheManager != null, "No CacheManager set");
        return this.cacheManager;
    }

    @Override
    public void afterPropertiesSet()  {
        Assert.notNull(this.cacheManager, "CacheManager is required");
    }

    @Override
    public Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {
        Collection<String> cacheNames = getCacheNames(context);
        if (cacheNames == null) {
            return Collections.emptyList();
        }
        Collection<Cache> result = new ArrayList<>(cacheNames.size());
        for (String cacheName : cacheNames) {
            Cache cache = getCacheManager().getCache(cacheName);
            if (cache == null) {
                throw new IllegalArgumentException("Cannot find cache named '" +
                        cacheName + "' for " + context.getOperation());
            }
            result.add(cache);
        }
        return result;
    }

    /**
     * Provide the name of the cache(s) to resolve against the current cache manager.
     * It is acceptable to return null to indicate
     * that no cache could be resolved for this invocation.
     */
    protected abstract Collection<String> getCacheNames(CacheOperationInvocationContext<?> context);

}
