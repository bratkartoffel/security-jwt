/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.service;

import eu.fraho.spring.securityJwt.config.JwtRefreshConfiguration;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RegisterRefreshTokenStore implements InitializingBean {
    public static final String BEAN_NAME = "refreshTokenStore";

    @NonNull
    private final ConfigurableListableBeanFactory factory;

    @NonNull
    private final JwtRefreshConfiguration refreshConfig;

    @Setter
    private BeanDefinitionRegistry registry;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (registry == null) registry = ((BeanDefinitionRegistry) factory);

        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(refreshConfig.getCacheImpl());
        beanDefinition.setLazyInit(false);
        beanDefinition.setAbstract(false);
        beanDefinition.setAutowireCandidate(true);
        beanDefinition.setScope(AbstractBeanDefinition.SCOPE_DEFAULT);

        registry.registerBeanDefinition(BEAN_NAME, beanDefinition);
    }
}
