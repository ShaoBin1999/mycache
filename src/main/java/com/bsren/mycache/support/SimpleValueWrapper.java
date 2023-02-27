package com.bsren.mycache.support;


import com.bsren.mycache.Cache;
import org.springframework.lang.Nullable;

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

