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

    class ValueRetrievalException extends RuntimeException {

        @Nullable
        private final Object key;

        public ValueRetrievalException(@Nullable Object key, Callable<?> loader, Throwable ex) {
            super(String.format("Value for key '%s' could not be loaded using '%s'", key, loader), ex);
            this.key = key;
        }

        @Nullable
        public Object getKey() {
            return this.key;
        }
    }

    @Nullable
    <T> T get(Object key, Callable<T> valueLoader);

    @Nullable
    default Cache.ValueWrapper putIfAbsent(Object key, @Nullable Object value) {
        Cache.ValueWrapper existingValue = get(key);
        if (existingValue == null) {
            put(key, value);
        }
        return existingValue;
    }
}
