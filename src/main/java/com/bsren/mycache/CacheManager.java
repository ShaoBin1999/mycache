package com.bsren.mycache;

import java.util.Collection;

public interface CacheManager {

    /**
     * Note that the cache may be lazily created at runtime if the native provider supports it.
     */
    Cache getCache(String name);

    Collection<String> getCacheNames();
}
