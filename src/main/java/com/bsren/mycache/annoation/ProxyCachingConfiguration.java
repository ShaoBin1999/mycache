package com.bsren.mycache.annoation;

import com.bsren.mycache.config.CacheManagementConfigUtils;
import com.bsren.mycache.interceptor.BeanFactoryCacheOperationSourceAdvisor;
import com.bsren.mycache.interceptor.CacheOperationSource;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.cache.interceptor.CacheInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

@Configuration(proxyBeanMethods = false)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class ProxyCachingConfiguration extends AbstractCachingConfiguration{

    @Bean(name = CacheManagementConfigUtils.CACHE_ADVISOR_BEAN_NAME)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public BeanFactoryCacheOperationSourceAdvisor cacheAdvisor(
            CacheOperationSource cacheOperationSource,
            CacheInterceptor cacheInterceptor) {

        BeanFactoryCacheOperationSourceAdvisor advisor = new BeanFactoryCacheOperationSourceAdvisor();
        advisor.setCacheOperationSource(cacheOperationSource);
        advisor.setAdvice(cacheInterceptor);
        if (this.enableCaching != null) {
            advisor.setOrder(this.enableCaching.<Integer>getNumber("order"));
        }
        return advisor;
    }
}
