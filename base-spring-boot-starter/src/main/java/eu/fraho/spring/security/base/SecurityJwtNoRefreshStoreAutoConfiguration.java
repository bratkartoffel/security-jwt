/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.security.base;

import eu.fraho.spring.security.base.service.NullTokenStore;
import eu.fraho.spring.security.base.service.RefreshTokenStore;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter(SecurityJwtBaseAutoConfiguration.class)
public class SecurityJwtNoRefreshStoreAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public RefreshTokenStore refreshTokenStore() {
        return new NullTokenStore();
    }
}
