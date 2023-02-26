package com.bsren.mycache;

import java.util.Collection;

public interface CacheManager {

    Cache getCache(String name);

    Collection<String> getCacheNames();
}
