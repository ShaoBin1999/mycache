package com.bsren.mycache;

import org.springframework.lang.Nullable;

import java.util.concurrent.Callable;

public interface Cache {

    String getName();

    Object getNativeCache();

    @Nullable
    <T> T get(Object key, @Nullable Class<T> type);

    void put(Object key, @Nullable Object value);

    void evict(Object key);

    void clear();

    @FunctionalInterface
    interface ValueWrapper {

        /**
         * Return the actual value in the cache.
         */
        @Nullable
        Object get();
    }

    @Nullable
    ValueWrapper get(Object key);

    default boolean evictIfPresent(Object key) {
        evict(key);
        return false;
    }

    default boolean invalidate() {
        clear();
        return false;
    }

}
