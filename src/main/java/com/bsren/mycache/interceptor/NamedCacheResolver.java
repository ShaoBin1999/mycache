package com.bsren.mycache.interceptor;

import com.bsren.mycache.CacheManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class NamedCacheResolver extends AbstractCacheResolver{

    private Collection<String> cacheNames;

    public NamedCacheResolver(){}

    public NamedCacheResolver(CacheManager cacheManager, String... cacheNames){
        super(cacheManager);
        this.cacheNames = new ArrayList<>(Arrays.asList(cacheNames));
    }

    @Override
    protected Collection<String> getCacheNames(CacheOperationInvocationContext<?> context) {
        return this.cacheNames;
    }

    public void setCacheNames(Collection<String> cacheNames) {
        this.cacheNames = cacheNames;
    }
}
