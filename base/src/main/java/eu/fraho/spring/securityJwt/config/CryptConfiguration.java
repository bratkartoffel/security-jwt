/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.config;

import eu.fraho.spring.securityJwt.dto.CryptAlgorithm;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "fraho.crypt")
@Component
@Data
@Slf4j
public class CryptConfiguration implements InitializingBean {
    private int rounds = 10_000;
    private CryptAlgorithm algorithm = CryptAlgorithm.valueOf("SHA512");

    @Override
    public void afterPropertiesSet() {
        if (algorithm.isRoundsSupported()) {
            if (rounds < 100 || rounds > 500_000) {
                log.warn("Encryption rounds out of bounds ({} <= {} <= {}), forcing to default ({})",
                        100, rounds, 500_000, 10_000);
                rounds = 10_000;
            }
        }
        if (algorithm.isInsecure()) {
            log.warn("Using insecure crypt variant {}. Consider upgrading to a stronger one.", algorithm);
        }
    }
}
