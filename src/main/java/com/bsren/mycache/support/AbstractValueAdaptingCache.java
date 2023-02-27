package com.bsren.mycache.support;

import com.bsren.mycache.Cache;
import org.springframework.lang.Nullable;

/**
 * 接受null值
 */
public abstract class AbstractValueAdaptingCache implements Cache {

    private final boolean allowNullValues;

    protected AbstractValueAdaptingCache(boolean allowNullValues) {
        this.allowNullValues = allowNullValues;
    }

    public final boolean isAllowNullValues() {
        return this.allowNullValues;
    }

    @Override
    @Nullable
    public Cache.ValueWrapper get(Object key) {
        return toValueWrapper(lookup(key));
    }

    @Override
    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T get(Object key, @Nullable Class<T> type) {
        Object value = fromStoreValue(lookup(key));
        if (value != null && type != null && !type.isInstance(value)) {
            throw new IllegalStateException(
                    "Cached value is not of required type [" + type.getName() + "]: " + value);
        }
        return (T) value;
    }

    /**
     * Perform an actual lookup in the underlying store.
     * @param key the key whose associated value is to be returned
     * @return the raw store value for the key, or {@code null} if none
     */
    @Nullable
    protected abstract Object lookup(Object key);


    /**
     * Convert the given value from the internal store to a user value
     * returned from the get method (adapting {@code null}).
     * @param storeValue the store value
     * @return the value to return to the user
     */
    @Nullable
    protected Object fromStoreValue(@Nullable Object storeValue) {
        if (this.allowNullValues && storeValue == NullValue.INSTANCE) {
            return null;
        }
        return storeValue;
    }

    /**
     * Convert the given user value, as passed into the put method,
     * to a value in the internal store (adapting {@code null}).
     * @param userValue the given user value
     * @return the value to store
     */
    protected Object toStoreValue(@Nullable Object userValue) {
        if (userValue == null) {
            if (this.allowNullValues) {
                return NullValue.INSTANCE;
            }
            throw new IllegalArgumentException(
                    "Cache '" + getName() + "' is configured to not allow null values but null was provided");
        }
        return userValue;
    }

    /**
     * Wrap the given store value with a {@link SimpleValueWrapper}, also going
     * through {@link #fromStoreValue} conversion. Useful for {@link #get(Object)}
     * and {@link #putIfAbsent(Object, Object)} implementations.
     * @param storeValue the original value
     * @return the wrapped value
     */
    @Nullable
    protected Cache.ValueWrapper toValueWrapper(@Nullable Object storeValue) {
        return (storeValue != null ? new SimpleValueWrapper(fromStoreValue(storeValue)) : null);
    }
}
