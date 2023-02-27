package com.bsren.mycache.concurent;

import com.bsren.mycache.Cache;
import com.bsren.mycache.CacheManager;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.core.serializer.support.SerializationDelegate;
import org.springframework.lang.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConcurrentMapCacheManager implements CacheManager, BeanClassLoaderAware {

    private final ConcurrentMap<String, Cache> cacheMap = new ConcurrentHashMap<>(16);

    private boolean dynamic = true;

    private boolean allowNullValues = true;

    private boolean storeByValue = false;

    public ConcurrentMapCacheManager() {
    }

    public ConcurrentMapCacheManager(String... cacheNames) {
        setCacheNames(Arrays.asList(cacheNames));
    }

    public void setCacheNames(@Nullable Collection<String> cacheNames) {
        if (cacheNames != null) {
            for (String name : cacheNames) {
                this.cacheMap.put(name, createConcurrentMapCache(name));
            }
            this.dynamic = false;
        }
        else {
            this.dynamic = true;
        }
    }


    public void setStoreByValue(boolean storeByValue) {
        if (storeByValue != this.storeByValue) {
            this.storeByValue = storeByValue;
            // Need to recreate all Cache instances with the new store-by-value configuration...
            recreateCaches();
        }
    }

    /**
     * Return whether this cache manager accepts and converts {@code null} values
     * for all of its caches.
     */
    public boolean isAllowNullValues() {
        return this.allowNullValues;
    }


    @Nullable
    private SerializationDelegate serialization;

    /**
     * Specify whether to accept and convert {@code null} values for all caches
     * in this cache manager.
     * <p>Default is "true", despite ConcurrentHashMap itself not supporting {@code null}
     * values. An internal holder object will be used to store user-level {@code null}s.
     * <p>Note: A change of the null-value setting will reset all existing caches,
     * if any, to reconfigure them with the new null-value requirement.
     */
    public void setAllowNullValues(boolean allowNullValues) {
        if (allowNullValues != this.allowNullValues) {
            this.allowNullValues = allowNullValues;
            // Need to recreate all Cache instances with the new null-value configuration...
            recreateCaches();
        }
    }

    private void recreateCaches() {
        for (Map.Entry<String, Cache> entry : this.cacheMap.entrySet()) {
            entry.setValue(createConcurrentMapCache(entry.getKey()));
        }
    }




    @Override
    @Nullable
    public Cache getCache(String name) {
        Cache cache = this.cacheMap.get(name);
        if (cache == null && this.dynamic) {
            synchronized (this.cacheMap) {
                cache = this.cacheMap.get(name);
                if (cache == null) {
                    cache = createConcurrentMapCache(name);
                    this.cacheMap.put(name, cache);
                }
            }
        }
        return cache;
    }

    @Override
    public Collection<String> getCacheNames() {
        return Collections.unmodifiableSet(this.cacheMap.keySet());
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.serialization = new SerializationDelegate(classLoader);
        // Need to recreate all Cache instances with new ClassLoader in store-by-value mode...
        if (isStoreByValue()) {
            recreateCaches();
        }
    }



    /**
     * Return whether this cache manager stores a copy of each entry or
     * a reference for all its caches. If store by value is enabled, any
     * cache entry must be serializable.
     * @since 4.3
     */
    public boolean isStoreByValue() {
        return this.storeByValue;
    }

    protected Cache createConcurrentMapCache(String name) {
        SerializationDelegate actualSerialization = (isStoreByValue() ? this.serialization : null);
        return new ConcurrentMapCache(name, new ConcurrentHashMap<>(256), isAllowNullValues(), actualSerialization);
    }
}
