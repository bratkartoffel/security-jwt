/*
 * MIT Licence
 * Copyright (c) 2026 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.service;

import eu.fraho.libs.totp.Totp;
import eu.fraho.libs.totp.TotpSettings;
import eu.fraho.spring.securityJwt.base.config.TotpProperties;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base32;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class TotpServiceImpl implements TotpService, InitializingBean {
    private final Base32 base32 = new Base32();
    private TotpProperties totpProperties;
    private Totp totp;

    @Override
    public boolean verifyCode(String secret, int code) {
        return totp.verifyCode(base32.decode(secret), code);
    }

    @Override
    public String generateSecret() {
        return base32.encodeToString(totp.generateSecret());
    }

    @Autowired
    public void setTotpProperties(@NonNull TotpProperties totpProperties) {
        this.totpProperties = totpProperties;
    }

    @Override
    public void afterPropertiesSet() {
        totp = new Totp(TotpSettings.builder()
                .variance(totpProperties.getVariance())
                .secretLength(totpProperties.getLength())
                .build()
        );
    }
}
