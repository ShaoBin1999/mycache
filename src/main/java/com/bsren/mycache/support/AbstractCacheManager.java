package com.bsren.mycache.support;

import com.bsren.mycache.Cache;
import com.bsren.mycache.CacheManager;
import org.springframework.beans.factory.InitializingBean;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * cache是一个大的概念，应该叫做cache组
 * 同一个组的cache在一个map里面，所有的cache被cacheMap所管理
 *
 * 在属性注入后，loadCache获得所有的cache，然后注册到cacheMap和cacheNames中；或者re-initialize at runtime
 *
 * getCache可能会因为懒加载而找不到，这时候需要重新
 * 但是！这个功能是提供给后面版本的
 *
 * 适用于static environment，因为cache一但加载后是不变的
 */

public abstract class AbstractCacheManager
    implements CacheManager,InitializingBean {

    private final ConcurrentMap<String, Cache> cacheMap = new ConcurrentHashMap<>();

    private volatile Set<String> cacheNames = Collections.emptySet();

    @Override
    public void afterPropertiesSet() throws Exception {
        initializeCaches();
    }

    /**
     * Triggered on startup through afterPropertiesSet();
     * can also be called to re-initialize at runtime.
     */
    private void initializeCaches() {
        Collection<? extends Cache> caches = loadCaches();
        synchronized (this.cacheMap){
            this.cacheNames = Collections.emptySet();
            this.cacheMap.clear();
            Set<String> cacheNames = new LinkedHashSet<>(caches.size());
            for (Cache cache : caches) {
                String name = cache.getName();
                this.cacheMap.put(name,cache);
                cacheNames.add(name);
            }
        }
        this.cacheNames = Collections.unmodifiableSet(cacheNames);
    }

    /**
     * The returned collection may be empty but must not be {@code null}.
     */
    protected abstract Collection<? extends Cache> loadCaches();

    @Override
    public Cache getCache(String name) {
        // Quick check for existing cache...
        Cache cache = this.cacheMap.get(name);
        if (cache != null) {
            return cache;
        }

        // The provider may support on-demand cache creation...
        Cache missingCache = getMissingCache(name);
        if (missingCache != null) {
            // Fully synchronize now for missing cache registration
            synchronized (this.cacheMap) {
                cache = this.cacheMap.get(name);
                if (cache == null) {
                    cache = decorateCache(missingCache);
                    this.cacheMap.put(name, cache);
                    updateCacheNames(name);
                }
            }
        }
        return cache;
    }

    @Override
    public Set<String> getCacheNames() {
        return this.cacheNames;
    }

    private void updateCacheNames(String name) {
        Set<String> cacheNames = new LinkedHashSet<>(this.cacheNames.size() + 1);
        cacheNames.addAll(this.cacheNames);
        cacheNames.add(name);
        this.cacheNames = Collections.unmodifiableSet(cacheNames);
    }

    protected Cache decorateCache(Cache cache) {
        return cache;
    }

    /**
     * Return a missing cache with the specified name,
     * or null if such a cache does not exist or could not be created on demand.
     * Caches may be lazily created at runtime if the native provider supports it.
     * If a lookup by name does not yield any result,
     * an AbstractCacheManager subclass gets a chance to register such a cache at runtime.
     * The returned cache will be automatically added to this cache manager.
     */
    protected Cache getMissingCache(String name) {
        return null;
    }
}
