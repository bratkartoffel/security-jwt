/*
 * MIT Licence
 * Copyright (c) 2025 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.service;

import eu.fraho.spring.securityJwt.base.config.RefreshProperties;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@NoArgsConstructor
public class RegisterRefreshTokenStore implements InitializingBean {
    public static final String BEAN_NAME = "refreshTokenStore";

    private ConfigurableListableBeanFactory factory;
    private RefreshProperties refreshProperties;
    private BeanDefinitionRegistry registry;

    @Override
    public void afterPropertiesSet() {
        if (registry == null) setRegistry((BeanDefinitionRegistry) factory);

        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(refreshProperties.getCacheImpl());
        beanDefinition.setLazyInit(false);
        beanDefinition.setAbstract(false);
        beanDefinition.setAutowireCandidate(true);
        beanDefinition.setScope(AbstractBeanDefinition.SCOPE_DEFAULT);

        log.info("Registering RefreshTokenStore = {}", refreshProperties.getCacheImpl());
        registry.registerBeanDefinition(BEAN_NAME, beanDefinition);
    }

    @Autowired
    public void setFactory(@NonNull ConfigurableListableBeanFactory factory) {
        this.factory = factory;
    }

    @Autowired
    public void setRefreshProperties(@NonNull RefreshProperties refreshProperties) {
        this.refreshProperties = refreshProperties;
    }

    public void setRegistry(@NonNull BeanDefinitionRegistry registry) {
        this.registry = registry;
    }
}
