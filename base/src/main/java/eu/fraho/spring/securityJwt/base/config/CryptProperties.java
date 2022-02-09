/*
 * MIT Licence
 * Copyright (c) 2022 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.config;

import eu.fraho.spring.securityJwt.base.dto.CryptAlgorithm;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "fraho.crypt")
@Component
@Getter
@Setter
@Slf4j
public class CryptProperties implements InitializingBean {
    /**
     * Defines the "strength" of the hashing function. The more rounds used, the more secure the generated hash.
     * But beware that more rounds mean more cpu-load and longer computation times!
     * This parameter is only used if the specified algorithm supports hashing rounds.
     */
    private int rounds = 50_000;

    /**
     * Defines the "cost" of the hashing function when using blowfish. The higher the cost, the more secure the generated hash.
     * But beware that a higher cost means more cpu-load and longer computation times!
     */
    private int cost = 12;

    /**
     * Configure the used crypt algorithm. Please be aware that changing this parameter has a major effect on the
     * strength of the hashed password! Do not use insecure algorithms (as DES or MD5 as time of writing) unless
     * you really know what you do!
     */
    private CryptAlgorithm algorithm = CryptAlgorithm.SHA512;

    @Override
    public void afterPropertiesSet() {
        if (algorithm.isRoundsSupported() && (rounds < 10_000 || rounds > 100_000_000)) {
            log.warn("Encryption rounds out of bounds ({} <= {} <= {}), forcing to default ({})",
                    10_000, rounds, 100_000_000, 50_000);
            rounds = 50_000;
        }
        if (CryptAlgorithm.BLOWFISH.equals(algorithm) && (cost < 12 || cost > 19)) {
            log.warn("Encryption cost out of bounds ({} <= {} <= {}), forcing to default ({})",
                    10, cost, 19, 12);
            cost = 12;
        }
        if (algorithm.isInsecure()) {
            log.warn("Using insecure crypt variant {}. Consider upgrading to a stronger one.", algorithm);
        }
    }
}
