/*
 * MIT Licence
 * Copyright (c) 2025 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "fraho.totp")
@Component
@Getter
@Setter
@Slf4j
public class TotpProperties implements InitializingBean {
    /**
     * Sets the allowed variance for the token checking algorithm. As the TOTP algorithm heavily depends on synchronized clocks
     * between client and server (and this is not always possible / within our control) this parameter can be tuned
     * to widen the allowed 30-seconds windows of tokens.
     * The default of 3 should be good for the normal cases (accepts tokens within the last 3 30-seconds windows (90 seconds).
     */
    private int variance = 3;

    /**
     * The length of generated TOTP secrets in bytes. The longer the secret, the more secure is this algorithm. The default
     * of 16 should be a sane and secure value for most cases.
     */
    private int length = 16;

    @Override
    public void afterPropertiesSet() {
        if (variance < 1 || variance > 20) {
            log.warn("TOTP variance out of bounds ({} <= {} <= {}), forcing to default ({})",
                    1, variance, 20, 3);
            variance = 3;
        }
        if (length < 8 || length > 64) {
            log.warn("TOTP length out of bounds ({} <= {} <= {}), forcing to default ({})",
                    8, length, 64, 16);
            length = 16;
        }
    }
}
