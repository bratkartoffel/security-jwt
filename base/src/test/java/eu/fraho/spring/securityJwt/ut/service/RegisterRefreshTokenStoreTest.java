/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.ut.service;

import eu.fraho.spring.securityJwt.config.RefreshProperties;
import eu.fraho.spring.securityJwt.service.RegisterRefreshTokenStore;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

public class RegisterRefreshTokenStoreTest {
    @Test
    public void testRegistration() throws Exception {
        ConfigurableListableBeanFactory factory = Mockito.mock(ConfigurableListableBeanFactory.class);
        BeanDefinitionRegistry registry = Mockito.mock(BeanDefinitionRegistry.class);
        RefreshProperties refreshConfig = new RefreshProperties();
        RegisterRefreshTokenStore service = new RegisterRefreshTokenStore(factory, refreshConfig);
        service.setRegistry(registry);
        service.afterPropertiesSet();

        Mockito.verify(registry, Mockito.times(1))
                .registerBeanDefinition(Matchers.eq("refreshTokenStore"), Matchers.any());
    }
}
