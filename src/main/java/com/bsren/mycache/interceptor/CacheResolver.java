package com.bsren.mycache.interceptor;

import com.bsren.mycache.Cache;

import java.util.Collection;

@FunctionalInterface
public interface CacheResolver {

    Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context);
}
