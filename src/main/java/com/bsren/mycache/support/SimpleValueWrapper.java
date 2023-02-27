package com.bsren.mycache.support;


import com.bsren.mycache.Cache;
import org.springframework.lang.Nullable;

/**
 * 简单包装，用value来代替
 */
public class SimpleValueWrapper implements Cache.ValueWrapper {

    @Nullable
    private final Object value;

    public SimpleValueWrapper(@Nullable Object value) {
        this.value = value;
    }

    @Override
    @Nullable
    public Object get() {
        return this.value;
    }
}

