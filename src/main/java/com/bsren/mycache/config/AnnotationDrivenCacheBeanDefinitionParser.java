package com.bsren.mycache.config;

import com.bsren.mycache.interceptor.BeanFactoryCacheOperationSourceAdvisor;
import com.bsren.mycache.interceptor.CacheInterceptor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

public class AnnotationDrivenCacheBeanDefinitionParser implements BeanDefinitionParser {

    private static final String CACHE_ASPECT_CLASS_NAME =
            "org.springframework.cache.aspectj.AnnotationCacheAspect";

    private static final String JCACHE_ASPECT_CLASS_NAME =
            "org.springframework.cache.aspectj.JCacheCacheAspect";

    private static final boolean jsr107Present;

    private static final boolean jcacheImplPresent;

    static {
        ClassLoader classLoader = AnnotationDrivenCacheBeanDefinitionParser.class.getClassLoader();
        jsr107Present = ClassUtils.isPresent("javax.cache.Cache", classLoader);
        jcacheImplPresent = ClassUtils.isPresent(
                "org.springframework.cache.jcache.interceptor.DefaultJCacheOperationSource", classLoader);
    }


    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        return null;
    }


    /**
     * Parse the cache resolution strategy to use. If a 'cache-resolver' attribute
     * is set, it is injected. Otherwise the 'cache-manager' is set. If {@code setBoth}
     * is {@code true}, both service are actually injected.
     */
    private static void parseCacheResolution(Element element, BeanDefinition def, boolean setBoth) {
        String name = element.getAttribute("cache-resolver");
        boolean hasText = StringUtils.hasText(name);
        if (hasText) {
            def.getPropertyValues().add("cacheResolver", new RuntimeBeanReference(name.trim()));
        }
        if (!hasText || setBoth) {
            def.getPropertyValues().add("cacheManager",
                    new RuntimeBeanReference(CacheNamespaceHandler.extractCacheManager(element)));
        }
    }


    private static void parseErrorHandler(Element element, BeanDefinition def) {
        String name = element.getAttribute("error-handler");
        if (StringUtils.hasText(name)) {
            def.getPropertyValues().add("errorHandler", new RuntimeBeanReference(name.trim()));
        }
    }



    private static class SpringCachingConfigurer {

        private static void registerCacheAdvisor(Element element, ParserContext parserContext) {
            if (!parserContext.getRegistry().containsBeanDefinition(CacheManagementConfigUtils.CACHE_ADVISOR_BEAN_NAME)) {
                Object eleSource = parserContext.extractSource(element);

                // Create the CacheOperationSource definition.
                RootBeanDefinition sourceDef = new RootBeanDefinition("org.springframework.cache.annotation.AnnotationCacheOperationSource");
                sourceDef.setSource(eleSource);
                sourceDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
                String sourceName = parserContext.getReaderContext().registerWithGeneratedName(sourceDef);

                // Create the CacheInterceptor definition.
                RootBeanDefinition interceptorDef = new RootBeanDefinition(CacheInterceptor.class);
                interceptorDef.setSource(eleSource);
                interceptorDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
                parseCacheResolution(element, interceptorDef, false);
                parseErrorHandler(element, interceptorDef);
                CacheNamespaceHandler.parseKeyGenerator(element, interceptorDef);
                interceptorDef.getPropertyValues().add("cacheOperationSources", new RuntimeBeanReference(sourceName));
                String interceptorName = parserContext.getReaderContext().registerWithGeneratedName(interceptorDef);

                // Create the CacheAdvisor definition.
                RootBeanDefinition advisorDef = new RootBeanDefinition(BeanFactoryCacheOperationSourceAdvisor.class);
                advisorDef.setSource(eleSource);
                advisorDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
                advisorDef.getPropertyValues().add("cacheOperationSource", new RuntimeBeanReference(sourceName));
                advisorDef.getPropertyValues().add("adviceBeanName", interceptorName);
                if (element.hasAttribute("order")) {
                    advisorDef.getPropertyValues().add("order", element.getAttribute("order"));
                }
                parserContext.getRegistry().registerBeanDefinition(CacheManagementConfigUtils.CACHE_ADVISOR_BEAN_NAME, advisorDef);

                CompositeComponentDefinition compositeDef = new CompositeComponentDefinition(element.getTagName(), eleSource);
                compositeDef.addNestedComponent(new BeanComponentDefinition(sourceDef, sourceName));
                compositeDef.addNestedComponent(new BeanComponentDefinition(interceptorDef, interceptorName));
                compositeDef.addNestedComponent(new BeanComponentDefinition(advisorDef, CacheManagementConfigUtils.CACHE_ADVISOR_BEAN_NAME));
                parserContext.registerComponent(compositeDef);
            }
        }

        /**
         * Registers a cache aspect.
         * <pre class="code">
         * &lt;bean id="cacheAspect" class="org.springframework.cache.aspectj.AnnotationCacheAspect" factory-method="aspectOf"&gt;
         *   &lt;property name="cacheManager" ref="cacheManager"/&gt;
         *   &lt;property name="keyGenerator" ref="keyGenerator"/&gt;
         * &lt;/bean&gt;
         * </pre>
         */
        private static void registerCacheAspect(Element element, ParserContext parserContext) {
            if (!parserContext.getRegistry().containsBeanDefinition(CacheManagementConfigUtils.CACHE_ASPECT_BEAN_NAME)) {
                RootBeanDefinition def = new RootBeanDefinition();
                def.setBeanClassName(CACHE_ASPECT_CLASS_NAME);
                def.setFactoryMethodName("aspectOf");
                parseCacheResolution(element, def, false);
                CacheNamespaceHandler.parseKeyGenerator(element, def);
                parserContext.registerBeanComponent(new BeanComponentDefinition(def, CacheManagementConfigUtils.CACHE_ASPECT_BEAN_NAME));
            }
        }
    }
}
