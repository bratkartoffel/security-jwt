/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.starter;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

import java.security.Security;

@Slf4j
@Configuration
@ConditionalOnClass(BouncyCastleProvider.class)
@AutoConfigureBefore(SecurityJwtBaseAutoConfiguration.class)
public class InstallBouncyCastleConfiguration implements InitializingBean {
    @Override
    public void afterPropertiesSet() {
        log.debug("Register BouncyCastleProvider");
        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.removeProvider(provider.getName());
        Security.addProvider(provider);
    }
}
