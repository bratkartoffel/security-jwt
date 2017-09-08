/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.security.base.ut.service;

import eu.fraho.spring.security.base.config.JwtRefreshConfiguration;
import eu.fraho.spring.security.base.service.RegisterRefreshTokenStore;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

public class TestRegisterRefreshTokenStore {
    @Test
    public void testRegistration() throws Exception {
        ConfigurableListableBeanFactory factory = Mockito.mock(ConfigurableListableBeanFactory.class);
        BeanDefinitionRegistry registry = Mockito.mock(BeanDefinitionRegistry.class);
        JwtRefreshConfiguration refreshConfig = new JwtRefreshConfiguration();
        RegisterRefreshTokenStore service = new RegisterRefreshTokenStore(factory, refreshConfig);
        service.setRegistry(registry);
        service.afterPropertiesSet();

        Mockito.verify(registry, Mockito.times(1))
                .registerBeanDefinition(Matchers.eq("refreshTokenStore"), Matchers.any());
    }
}
