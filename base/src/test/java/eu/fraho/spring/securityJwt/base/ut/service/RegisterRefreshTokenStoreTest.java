/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.ut.service;

import eu.fraho.spring.securityJwt.base.config.RefreshProperties;
import eu.fraho.spring.securityJwt.base.service.RegisterRefreshTokenStore;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

public class RegisterRefreshTokenStoreTest {
    @Test
    public void testRegistration() {
        ConfigurableListableBeanFactory factory = Mockito.mock(ConfigurableListableBeanFactory.class);
        BeanDefinitionRegistry registry = Mockito.mock(BeanDefinitionRegistry.class);
        RefreshProperties refreshProperties = new RefreshProperties();
        RegisterRefreshTokenStore service = new RegisterRefreshTokenStore();
        service.setFactory(factory);
        service.setRefreshProperties(refreshProperties);
        service.setRegistry(registry);
        service.afterPropertiesSet();

        Mockito.verify(registry, Mockito.times(1))
                .registerBeanDefinition(ArgumentMatchers.eq("refreshTokenStore"), ArgumentMatchers.any());
    }
}
