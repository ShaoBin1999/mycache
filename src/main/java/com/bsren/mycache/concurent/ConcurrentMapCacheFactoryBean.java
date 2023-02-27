package com.bsren.mycache.concurent;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.util.concurrent.ConcurrentMap;

public class ConcurrentMapCacheFactoryBean implements FactoryBean<ConcurrentMapCache>, BeanNameAware, InitializingBean {

    private String name = "";

    @Nullable
    private ConcurrentMap<Object, Object> store;

    private boolean allowNullValues = true;

    @Nullable
    private ConcurrentMapCache cache;

    /**
     * Specify the name of the cache.
     * <p>Default is "" (empty String).
     */
    public void setName(String name) {
        this.name = name;
    }

    public void setStore(ConcurrentMap<Object, Object> store) {
        this.store = store;
    }

    public void setAllowNullValues(boolean allowNullValues) {
        this.allowNullValues = allowNullValues;
    }

    @Override
    public void setBeanName(String beanName) {
        if (!StringUtils.hasLength(this.name)) {
            setName(beanName);
        }
    }


    @Override
    @Nullable
    public ConcurrentMapCache getObject() {
        return this.cache;
    }

    @Override
    public Class<?> getObjectType() {
        return ConcurrentMapCache.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }


    @Override
    public void afterPropertiesSet() {
        this.cache = (this.store != null ?
                new ConcurrentMapCache(this.name, this.store, this.allowNullValues) :
                new ConcurrentMapCache(this.name, this.allowNullValues));
    }
}
