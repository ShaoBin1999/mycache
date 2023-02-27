package com.bsren.mycache.annoation;

import com.bsren.mycache.CacheManager;
import com.bsren.mycache.interceptor.CacheErrorHandler;
import com.bsren.mycache.interceptor.CacheResolver;
import com.bsren.mycache.interceptor.KeyGenerator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.function.SingletonSupplier;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @Configuration(proxyBeanMethods = false) 每次调用的时候都获取一个新的实例
 */
@Configuration(proxyBeanMethods = false)
public abstract class AbstractCachingConfiguration implements ImportAware {

    @Nullable
    protected AnnotationAttributes enableCaching;

    @Nullable
    protected Supplier<CacheManager> cacheManager;

    @Nullable
    protected Supplier<CacheResolver> cacheResolver;

    @Nullable
    protected Supplier<KeyGenerator> keyGenerator;

    @Nullable
    protected Supplier<CacheErrorHandler> errorHandler;


    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        this.enableCaching = AnnotationAttributes.fromMap(
                importMetadata.getAnnotationAttributes(EnableCaching.class.getName()));
        if (this.enableCaching == null) {
            throw new IllegalArgumentException(
                    "@EnableCaching is not present on importing class " + importMetadata.getClassName());
        }
    }

    @Autowired
    void setConfigurers(ObjectProvider<CachingConfigurer> configurers) {
        Supplier<CachingConfigurer> configurer = () -> {
            List<CachingConfigurer> candidates = configurers.stream().collect(Collectors.toList());
            if (CollectionUtils.isEmpty(candidates)) {
                return null;
            }
            if (candidates.size() > 1) {
                throw new IllegalStateException(candidates.size() + " implementations of " +
                        "CachingConfigurer were found when only 1 was expected. " +
                        "Refactor the configuration such that CachingConfigurer is " +
                        "implemented only once or not at all.");
            }
            return candidates.get(0);
        };
        useCachingConfigurer(new CachingConfigurerSupplier(configurer));
    }

    /**
     * Extract the configuration from the nominated {@link CachingConfigurer}.
     */
    protected void useCachingConfigurer(CachingConfigurerSupplier cachingConfigurerSupplier) {
        this.cacheManager = cachingConfigurerSupplier.adapt(CachingConfigurer::cacheManager);
        this.cacheResolver = cachingConfigurerSupplier.adapt(CachingConfigurer::cacheResolver);
        this.keyGenerator = cachingConfigurerSupplier.adapt(CachingConfigurer::keyGenerator);
        this.errorHandler = cachingConfigurerSupplier.adapt(CachingConfigurer::errorHandler);
    }


    protected static class CachingConfigurerSupplier {

        private final Supplier<CachingConfigurer> supplier;

        public CachingConfigurerSupplier(Supplier<CachingConfigurer> supplier) {
            this.supplier = SingletonSupplier.of(supplier);
        }

        /**
         * Adapt the {@link CachingConfigurer} supplier to another supplier
         * provided by the specified mapping function. If the underlying
         * {@link CachingConfigurer} is {@code null}, {@code null} is returned
         * and the mapping function is not invoked.
         * @param provider the provider to use to adapt the supplier
         * @param <T> the type of the supplier
         * @return another supplier mapped by the specified function
         */
        @Nullable
        public <T> Supplier<T> adapt(Function<CachingConfigurer, T> provider) {
            return () -> {
                CachingConfigurer cachingConfigurer = this.supplier.get();
                return (cachingConfigurer != null ? provider.apply(cachingConfigurer) : null);
            };
        }

    }
}
