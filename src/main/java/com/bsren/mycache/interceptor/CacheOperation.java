package com.bsren.mycache.interceptor;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class CacheOperation implements BasicOperation {

    private final String name;

    private final Set<String> cacheNames;

    private final String key;

    private final String keyGenerator;

    private final String cacheManager;

    private final String cacheResolver;

    private final String condition;

    private final String toString;

    protected CacheOperation(Builder b) {
        this.name = b.name;
        this.cacheNames = b.cacheNames;
        this.key = b.key;
        this.keyGenerator = b.keyGenerator;
        this.cacheManager = b.cacheManager;
        this.cacheResolver = b.cacheResolver;
        this.condition = b.condition;
        this.toString = b.getOperationDescription().toString();
    }

    public String getName() {
        return this.name;
    }

    @Override
    public Set<String> getCacheNames() {
        return this.cacheNames;
    }

    public String getKey() {
        return this.key;
    }

    public String getKeyGenerator() {
        return this.keyGenerator;
    }

    public String getCacheManager() {
        return this.cacheManager;
    }

    public String getCacheResolver() {
        return this.cacheResolver;
    }

    public String getCondition() {
        return this.condition;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return (other instanceof CacheOperation && toString().equals(other.toString()));
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public final String toString() {
        return this.toString;
    }


    public abstract static class Builder {

        private String name = "";

        private Set<String> cacheNames = Collections.emptySet();

        private String key = "";

        private String keyGenerator = "";

        private String cacheManager = "";

        private String cacheResolver = "";

        private String condition = "";

        public void setName(String name) {
            Assert.hasText(name, "Name must not be empty");
            this.name = name;
        }

        public void setCacheName(String cacheName) {
            Assert.hasText(cacheName, "Cache name must not be empty");
            this.cacheNames = Collections.singleton(cacheName);
        }

        public void setCacheNames(String... cacheNames) {
            this.cacheNames = new LinkedHashSet<>(cacheNames.length);
            for (String cacheName : cacheNames) {
                Assert.hasText(cacheName, "Cache name must be non-empty if specified");
                this.cacheNames.add(cacheName);
            }
        }

        public Set<String> getCacheNames() {
            return this.cacheNames;
        }

        public void setKey(String key) {
            Assert.notNull(key, "Key must not be null");
            this.key = key;
        }

        public String getKey() {
            return this.key;
        }

        public String getKeyGenerator() {
            return this.keyGenerator;
        }

        public String getCacheManager() {
            return this.cacheManager;
        }

        public String getCacheResolver() {
            return this.cacheResolver;
        }

        public void setKeyGenerator(String keyGenerator) {
            Assert.notNull(keyGenerator, "KeyGenerator name must not be null");
            this.keyGenerator = keyGenerator;
        }

        public void setCacheManager(String cacheManager) {
            Assert.notNull(cacheManager, "CacheManager name must not be null");
            this.cacheManager = cacheManager;
        }

        public void setCacheResolver(String cacheResolver) {
            Assert.notNull(cacheResolver, "CacheResolver name must not be null");
            this.cacheResolver = cacheResolver;
        }

        public void setCondition(String condition) {
            Assert.notNull(condition, "Condition must not be null");
            this.condition = condition;
        }

        /**
         * Return an identifying description for this caching operation.
         * <p>Available to subclasses, for inclusion in their {@code toString()} result.
         */
        protected StringBuilder getOperationDescription() {
            StringBuilder result = new StringBuilder(getClass().getSimpleName());
            result.append("[").append(this.name);
            result.append("] caches=").append(this.cacheNames);
            result.append(" | key='").append(this.key);
            result.append("' | keyGenerator='").append(this.keyGenerator);
            result.append("' | cacheManager='").append(this.cacheManager);
            result.append("' | cacheResolver='").append(this.cacheResolver);
            result.append("' | condition='").append(this.condition).append("'");
            return result;
        }

        public abstract CacheOperation build();
    }
}
