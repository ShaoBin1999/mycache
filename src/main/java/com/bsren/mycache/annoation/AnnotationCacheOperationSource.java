package com.bsren.mycache.annoation;



import com.bsren.mycache.interceptor.AbstractFallbackCacheOperationSource;
import com.bsren.mycache.interceptor.CacheOperation;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;

public class AnnotationCacheOperationSource  extends AbstractFallbackCacheOperationSource implements Serializable {


    private final boolean publicMethodsOnly;

    private final Set<CacheAnnotationParser> annotationParsers;

    public AnnotationCacheOperationSource() {
        this(true);
    }

    public AnnotationCacheOperationSource(boolean publicMethodsOnly) {
        this.publicMethodsOnly = publicMethodsOnly;
        this.annotationParsers = Collections.singleton(new SpringCacheAnnotationParser());
    }

    public AnnotationCacheOperationSource(CacheAnnotationParser annotationParser) {
        this.publicMethodsOnly = true;
        Assert.notNull(annotationParser, "CacheAnnotationParser must not be null");
        this.annotationParsers = Collections.singleton(annotationParser);
    }

    public AnnotationCacheOperationSource(CacheAnnotationParser... annotationParsers) {
        this.publicMethodsOnly = true;
        Assert.notEmpty(annotationParsers, "At least one CacheAnnotationParser needs to be specified");
        this.annotationParsers = new LinkedHashSet<>(Arrays.asList(annotationParsers));
    }

    public AnnotationCacheOperationSource(Set<CacheAnnotationParser> annotationParsers) {
        this.publicMethodsOnly = true;
        Assert.notEmpty(annotationParsers, "At least one CacheAnnotationParser needs to be specified");
        this.annotationParsers = annotationParsers;
    }

    @Override
    @Nullable
    protected Collection<CacheOperation> findCacheOperations(Class<?> clazz) {
//        return determineCacheOperations(parser -> parser.parseCacheAnnotations(clazz));
        return determineCacheOperations(new CacheOperationProvider() {
            @Override
            public Collection<CacheOperation> getCacheOperations(CacheAnnotationParser parser) {
                return parser.parseCacheAnnotations(clazz);
            }
        });
    }

    @Override
    @Nullable
    protected Collection<CacheOperation> findCacheOperations(Method method) {
        return determineCacheOperations(new CacheOperationProvider() {
            @Override
            public Collection<CacheOperation> getCacheOperations(CacheAnnotationParser parser) {
                return parser.parseCacheAnnotations(method);
            }
        });
    }

    @Nullable
    protected Collection<CacheOperation> determineCacheOperations(CacheOperationProvider provider) {
        Collection<CacheOperation> ops = null;
        for (CacheAnnotationParser parser : this.annotationParsers) {
            Collection<CacheOperation> annOps = provider.getCacheOperations(parser);
            if (annOps != null) {
                if (ops == null) {
                    ops = annOps;
                }
                else {
                    Collection<CacheOperation> combined = new ArrayList<>(ops.size() + annOps.size());
                    combined.addAll(ops);
                    combined.addAll(annOps);
                    ops = combined;
                }
            }
        }
        return ops;
    }

    @Override
    public boolean isCandidateClass(Class<?> targetClass) {
        for (CacheAnnotationParser parser : this.annotationParsers) {
            if (parser.isCandidateClass(targetClass)) {
                return true;
            }
        }
        return false;
    }


    @FunctionalInterface
    protected interface CacheOperationProvider {
        @Nullable
        Collection<CacheOperation> getCacheOperations(CacheAnnotationParser parser);
    }


}
