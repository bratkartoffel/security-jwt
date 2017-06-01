package eu.fraho.spring.securityJwt.service;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class RegisterRefreshTokenStore implements InitializingBean {
    public static final String BEAN_NAME = "refreshTokenStore";
    private static final String DEFAULT_TOKEN_STORE = "eu.fraho.spring.securityJwt.service.NullTokenStore";
    @Autowired
    private ApplicationContext applicationContext = null;

    @Autowired
    private ConfigurableListableBeanFactory factory = null;

    @Value("${fraho.jwt.refresh.cache.impl:" + DEFAULT_TOKEN_STORE + "}")
    private Class<? extends RefreshTokenStore> refreshTokenStoreImpl = null;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (refreshTokenStoreImpl == null) {
            refreshTokenStoreImpl = NullTokenStore.class;
        }
        BeanDefinitionRegistry registry = ((BeanDefinitionRegistry) factory);

        if (registry.containsBeanDefinition(BEAN_NAME)) {
            registry.removeBeanDefinition(BEAN_NAME);
        }
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(refreshTokenStoreImpl);
        beanDefinition.setLazyInit(false);
        beanDefinition.setAbstract(false);
        beanDefinition.setAutowireCandidate(true);
        beanDefinition.setScope(AbstractBeanDefinition.SCOPE_DEFAULT);

        registry.registerBeanDefinition(BEAN_NAME, beanDefinition);
    }

    public Class<? extends RefreshTokenStore> getRefreshTokenStoreImpl() {
        return refreshTokenStoreImpl;
    }
}
