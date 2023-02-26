package com.bsren.mycache.interceptor;

import com.bsren.mycache.Cache;
import com.bsren.mycache.CacheManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.cache.interceptor.CacheOperationInvoker;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.expression.EvaluationContext;
import org.springframework.lang.Nullable;
import org.springframework.util.*;
import org.springframework.util.function.SingletonSupplier;
import org.springframework.util.function.SupplierUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class CacheAspectSupport extends AbstractCacheInvoker
        implements BeanFactoryAware,
        InitializingBean,
        SmartInitializingSingleton {

    protected final Log logger = LogFactory.getLog(getClass());

    private final Map<CacheOperationCacheKey,CacheOperationMetadata> metadataCache = new ConcurrentHashMap<>(1024);

    private final CacheOperationExpressionEvaluator evaluator = new CacheOperationExpressionEvaluator();

    @Nullable
    private CacheOperationSource cacheOperationSource;

    private SingletonSupplier<KeyGenerator> keyGenerator = SingletonSupplier.of(SimpleKeyGenerator::new);

    @Nullable
    private SingletonSupplier<CacheResolver> cacheResolver;

    @Nullable
    private BeanFactory beanFactory;

    private boolean initialized = false;

    public void configure(
            @Nullable Supplier<CacheErrorHandler> errorHandler,
            @Nullable Supplier<KeyGenerator> keyGenerator,
            @Nullable Supplier<CacheResolver> cacheResolver,
            @Nullable Supplier<CacheManager> cacheManager) {

        this.errorHandler = new SingletonSupplier<>(errorHandler, SimpleCacheErrorHandler::new);
        this.keyGenerator = new SingletonSupplier<>(keyGenerator, SimpleKeyGenerator::new);
        this.cacheResolver = new SingletonSupplier<>(cacheResolver,
                () -> SimpleCacheResolver.of(SupplierUtils.resolve(cacheManager)));
    }

    public void setCacheOperationSources(CacheOperationSource... cacheOperationSources) {
        Assert.notEmpty(cacheOperationSources, "At least 1 CacheOperationSource needs to be specified");
        this.cacheOperationSource = (cacheOperationSources.length > 1 ?
                new CompositeCacheOperationSource(cacheOperationSources) : cacheOperationSources[0]);
    }


    public void setCacheOperationSource(@Nullable CacheOperationSource cacheOperationSource) {
        this.cacheOperationSource = cacheOperationSource;
    }

    @Nullable
    public CacheOperationSource getCacheOperationSource() {
        return this.cacheOperationSource;
    }

    public void setKeyGenerator(KeyGenerator keyGenerator) {
        this.keyGenerator = SingletonSupplier.of(keyGenerator);
    }


    public KeyGenerator getKeyGenerator() {
        return this.keyGenerator.obtain();
    }

    public void setCacheResolver(@Nullable CacheResolver cacheResolver) {
        this.cacheResolver = SingletonSupplier.ofNullable(cacheResolver);
    }

    @Nullable
    public CacheResolver getCacheResolver() {
        return SupplierUtils.resolve(this.cacheResolver);
    }

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheResolver = SingletonSupplier.of(new SimpleCacheResolver(cacheManager));
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public void afterPropertiesSet() {
        Assert.state(getCacheOperationSource() != null, "The 'cacheOperationSources' property is required: " +
                "If there are no cacheable methods, then don't use a cache aspect.");
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (getCacheResolver() == null) {
            // Lazily initialize cache resolver via default cache manager...
            Assert.state(this.beanFactory != null, "CacheResolver or BeanFactory must be set on cache aspect");
            try {
                setCacheManager(this.beanFactory.getBean(CacheManager.class));
            }
            catch (NoUniqueBeanDefinitionException ex) {
                throw new IllegalStateException("No CacheResolver specified, and no unique bean of type " +
                        "CacheManager found. Mark one as primary or declare a specific CacheManager to use.");
            }
            catch (NoSuchBeanDefinitionException ex) {
                throw new IllegalStateException("No CacheResolver specified, and no bean of type CacheManager found. " +
                        "Register a CacheManager bean or remove the @EnableCaching annotation from your configuration.");
            }
        }
        this.initialized = true;
    }

    protected String methodIdentification(Method method, Class<?> targetClass) {
        Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
        return ClassUtils.getQualifiedMethodName(specificMethod);
    }

    protected Collection<? extends Cache> getCaches(
            CacheOperationInvocationContext<CacheOperation> context, CacheResolver cacheResolver) {

        Collection<? extends Cache> caches = cacheResolver.resolveCaches(context);
        if (caches.isEmpty()) {
            throw new IllegalStateException("No cache could be resolved for '" +
                    context.getOperation() + "' using resolver '" + cacheResolver +
                    "'. At least one cache should be provided per cache operation.");
        }
        return caches;
    }

    protected CacheOperationContext getOperationContext(
            CacheOperation operation, Method method, Object[] args, Object target, Class<?> targetClass) {

        CacheOperationMetadata metadata = getCacheOperationMetadata(operation, method, targetClass);
        return new CacheOperationContext(metadata, args, target);
    }

    protected CacheOperationMetadata getCacheOperationMetadata(
            CacheOperation operation, Method method, Class<?> targetClass) {

        CacheOperationCacheKey cacheKey = new CacheOperationCacheKey(operation, method, targetClass);
        CacheOperationMetadata metadata = this.metadataCache.get(cacheKey);
        if (metadata == null) {
            KeyGenerator operationKeyGenerator;
            if (StringUtils.hasText(operation.getKeyGenerator())) {
                operationKeyGenerator = getBean(operation.getKeyGenerator(),KeyGenerator.class);
            }
            else {
                operationKeyGenerator = getKeyGenerator();
            }
            CacheResolver operationCacheResolver;
            if (StringUtils.hasText(operation.getCacheResolver())) {
                operationCacheResolver = getBean(operation.getCacheResolver(), CacheResolver.class);
            }
            else if (StringUtils.hasText(operation.getCacheManager())) {
                CacheManager cacheManager = getBean(operation.getCacheManager(), CacheManager.class);
                operationCacheResolver = new SimpleCacheResolver(cacheManager);
            }
            else {
                operationCacheResolver = getCacheResolver();
                Assert.state(operationCacheResolver != null, "No CacheResolver/CacheManager set");
            }
            metadata = new CacheAspectSupport.CacheOperationMetadata(operation, method, targetClass,
                    operationKeyGenerator, operationCacheResolver);
            this.metadataCache.put(cacheKey, metadata);
        }
        return metadata;
    }

    protected <T> T getBean(String beanName, Class<T> expectedType) {
        if (this.beanFactory == null) {
            throw new IllegalStateException(
                    "BeanFactory must be set on cache aspect for " + expectedType.getSimpleName() + " retrieval");
        }
        return BeanFactoryAnnotationUtils.qualifiedBeanOfType(this.beanFactory, expectedType, beanName);
    }

    protected void clearMetadataCache() {
        this.metadataCache.clear();
        this.evaluator.clear();
    }


    private Class<?> getTargetClass(Object target) {
        return AopProxyUtils.ultimateTargetClass(target);
    }


    private static final class CacheOperationCacheKey implements Comparable<CacheOperationCacheKey> {

        private final CacheOperation cacheOperation;

        private final AnnotatedElementKey methodCacheKey;

        private CacheOperationCacheKey(CacheOperation cacheOperation, Method method, Class<?> targetClass) {
            this.cacheOperation = cacheOperation;
            this.methodCacheKey = new AnnotatedElementKey(method, targetClass);
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof CacheOperationCacheKey)) {
                return false;
            }
            CacheOperationCacheKey otherKey = (CacheOperationCacheKey) other;
            return (this.cacheOperation.equals(otherKey.cacheOperation) &&
                    this.methodCacheKey.equals(otherKey.methodCacheKey));
        }

        @Override
        public int hashCode() {
            return (this.cacheOperation.hashCode() * 31 + this.methodCacheKey.hashCode());
        }

        @Override
        public String toString() {
            return this.cacheOperation + " on " + this.methodCacheKey;
        }

        @Override
        public int compareTo(CacheOperationCacheKey other) {
            int result = this.cacheOperation.getName().compareTo(other.cacheOperation.getName());
            if (result == 0) {
                result = this.methodCacheKey.compareTo(other.methodCacheKey);
            }
            return result;
        }
    }

    private class CacheOperationContexts {

        private final MultiValueMap<Class<? extends CacheOperation>, CacheOperationContext> contexts;

        private final boolean sync;

        public CacheOperationContexts(Collection<? extends CacheOperation> operations, Method method,
                                      Object[] args, Object target, Class<?> targetClass) {

            this.contexts = new LinkedMultiValueMap<>(operations.size());
            for (CacheOperation op : operations) {
                this.contexts.add(op.getClass(), getOperationContext(op, method, args, target, targetClass));
            }
            this.sync = determineSyncFlag(method);
        }

        public Collection<CacheAspectSupport.CacheOperationContext> get(Class<? extends CacheOperation> operationClass) {
            Collection<CacheOperationContext> result = this.contexts.get(operationClass);
            return (result != null ? result : Collections.emptyList());
        }

        public boolean isSynchronized() {
            return this.sync;
        }

        private boolean determineSyncFlag(Method method) {
            List<CacheOperationContext> cacheOperationContexts = this.contexts.get(CacheableOperation.class);
            if (cacheOperationContexts == null) {  // no @Cacheable operation at all
                return false;
            }
            boolean syncEnabled = false;
            for (CacheOperationContext cacheOperationContext : cacheOperationContexts) {
                if (((CacheableOperation) cacheOperationContext.getOperation()).isSync()) {
                    syncEnabled = true;
                    break;
                }
            }
            if (syncEnabled) {
                if (this.contexts.size() > 1) {
                    throw new IllegalStateException(
                            "@Cacheable(sync=true) cannot be combined with other cache operations on '" + method + "'");
                }
                if (cacheOperationContexts.size() > 1) {
                    throw new IllegalStateException(
                            "Only one @Cacheable(sync=true) entry is allowed on '" + method + "'");
                }
                CacheOperationContext cacheOperationContext = cacheOperationContexts.iterator().next();
                CacheableOperation operation = (CacheableOperation) cacheOperationContext.getOperation();
                if (cacheOperationContext.getCaches().size() > 1) {
                    throw new IllegalStateException(
                            "@Cacheable(sync=true) only allows a single cache on '" + operation + "'");
                }
                if (StringUtils.hasText(operation.getUnless())) {
                    throw new IllegalStateException(
                            "@Cacheable(sync=true) does not support unless attribute on '" + operation + "'");
                }
                return true;
            }
            return false;
        }
    }


    private class CachePutRequest {

        private final CacheOperationContext context;

        private final Object key;

        public CachePutRequest(CacheOperationContext context, Object key) {
            this.context = context;
            this.key = key;
        }

        public void apply(@Nullable Object result) {
            if (this.context.canPutToCache(result)) {
                for (Cache cache : this.context.getCaches()) {
                    doPut(cache, this.key, result);
                }
            }
        }
    }


    protected class CacheOperationContext implements CacheOperationInvocationContext<CacheOperation> {

        private final CacheOperationMetadata metadata;

        private final Object[] args;

        private final Object target;

        private final Collection<? extends Cache> caches;

        private final Collection<String> cacheNames;

        @Nullable
        private Boolean conditionPassing;

        public CacheOperationContext(CacheOperationMetadata metadata, Object[] args, Object target) {
            this.metadata = metadata;
            this.args = extractArgs(metadata.method, args);
            this.target = target;
            this.caches = CacheAspectSupport.this.getCaches(this, metadata.cacheResolver);
            this.cacheNames = createCacheNames(this.caches);
        }

        @Override
        public CacheOperation getOperation() {
            return this.metadata.operation;
        }

        @Override
        public Object getTarget() {
            return this.target;
        }

        @Override
        public Method getMethod() {
            return this.metadata.method;
        }

        @Override
        public Object[] getArgs() {
            return this.args;
        }

        private Object[] extractArgs(Method method, Object[] args) {
            if (!method.isVarArgs()) {
                return args;
            }
            Object[] varArgs = ObjectUtils.toObjectArray(args[args.length - 1]);
            Object[] combinedArgs = new Object[args.length - 1 + varArgs.length];
            System.arraycopy(args, 0, combinedArgs, 0, args.length - 1);
            System.arraycopy(varArgs, 0, combinedArgs, args.length - 1, varArgs.length);
            return combinedArgs;
        }

        protected boolean isConditionPassing(@Nullable Object result) {
            if (this.conditionPassing == null) {
                if (StringUtils.hasText(this.metadata.operation.getCondition())) {
                    EvaluationContext evaluationContext = createEvaluationContext(result);
                    this.conditionPassing = evaluator.condition(this.metadata.operation.getCondition(),
                            this.metadata.methodKey, evaluationContext);
                }
                else {
                    this.conditionPassing = true;
                }
            }
            return this.conditionPassing;
        }

        protected boolean canPutToCache(@Nullable Object value) {
            String unless = "";
            if (this.metadata.operation instanceof CacheableOperation) {
                unless = ((CacheableOperation) this.metadata.operation).getUnless();
            }
            else if (this.metadata.operation instanceof CachePutOperation) {
                unless = ((CachePutOperation) this.metadata.operation).getUnless();
            }
            if (StringUtils.hasText(unless)) {
                EvaluationContext evaluationContext = createEvaluationContext(value);
                return !evaluator.unless(unless, this.metadata.methodKey, evaluationContext);
            }
            return true;
        }

        @Nullable
        protected Object generateKey(@Nullable Object result) {
            if (StringUtils.hasText(this.metadata.operation.getKey())) {
                EvaluationContext evaluationContext = createEvaluationContext(result);
                return evaluator.key(this.metadata.operation.getKey(), this.metadata.methodKey, evaluationContext);
            }
            return this.metadata.keyGenerator.generate(this.target, this.metadata.method, this.args);
        }

        private EvaluationContext createEvaluationContext(@Nullable Object result) {
            return evaluator.createEvaluationContext(this.caches, this.metadata.method, this.args,
                    this.target, this.metadata.targetClass, this.metadata.targetMethod, result, beanFactory);
        }

        protected Collection<? extends Cache> getCaches() {
            return this.caches;
        }

        protected Collection<String> getCacheNames() {
            return this.cacheNames;
        }

        private Collection<String> createCacheNames(Collection<? extends Cache> caches) {
            Collection<String> names = new ArrayList<>();
            for (Cache cache : caches) {
                names.add(cache.getName());
            }
            return names;
        }
    }

    protected static class CacheOperationMetadata {

        private final CacheOperation operation;

        private final Method method;

        private final Class<?> targetClass;

        private final Method targetMethod;

        private final AnnotatedElementKey methodKey;

        private final KeyGenerator keyGenerator;

        private final CacheResolver cacheResolver;

        public CacheOperationMetadata(CacheOperation operation, Method method, Class<?> targetClass,
                                      KeyGenerator keyGenerator, CacheResolver cacheResolver) {

            this.operation = operation;
            this.method = BridgeMethodResolver.findBridgedMethod(method);
            this.targetClass = targetClass;
            this.targetMethod = (!Proxy.isProxyClass(targetClass) ?
                    AopUtils.getMostSpecificMethod(method, targetClass) : this.method);
            this.methodKey = new AnnotatedElementKey(this.targetMethod, targetClass);
            this.keyGenerator = keyGenerator;
            this.cacheResolver = cacheResolver;
        }
    }

    /**
     * Internal holder class for recording that a cache method was invoked.
     */
    private static class InvocationAwareResult {

        boolean invoked;

    }
}
