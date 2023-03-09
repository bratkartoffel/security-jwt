/*
 * MIT Licence
 * Copyright (c) 2022 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.service;

import eu.fraho.spring.securityJwt.base.config.TotpProperties;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base32;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

@Component
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class TotpServiceImpl implements TotpService {
    private final Base32 base32 = new Base32();

    private final Random random = new SecureRandom();

    private TotpProperties totpProperties;

    @Override
    public boolean verifyCode(String secret, int code) {
        long timeIndex = getTimeIndex();
        byte[] secretBytes = base32.decode(secret);
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(secretBytes, "HmacSHA1"));
            ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);

            for (int i = -totpProperties.getVariance(); i <= totpProperties.getVariance(); i++) {
                buffer.putLong(0, timeIndex + i);
                byte[] timeBytes = buffer.array();
                int calculated = createCode(mac, timeBytes);
                log.trace("Verifying code i={}, calculated={}, given={}", i, calculated, code);
                if (calculated == code) {
                    return true;
                }
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error checking totp pin", e);
        }

        return false;
    }

    public long getTimeIndex() {
        return System.currentTimeMillis() / 1000 / 30;
    }

    private int createCode(Mac mac, byte[] timeBytes) {
        byte[] hash = mac.doFinal(timeBytes);
        int offset = hash[19] & 0xf;
        return ((hash[offset] & 0x7f) << 24
                | (hash[offset + 1] & 0xff) << 16
                | (hash[offset + 2] & 0xff) << 8
                | (hash[offset + 3] & 0xff)) % 1000000;
    }

    @Override
    public String generateSecret() {
        final byte[] secret = new byte[totpProperties.getLength()];
        random.nextBytes(secret);
        log.debug("Generated secret with length={}", secret.length);
        return base32.encodeToString(secret);
    }

    @Autowired
    public void setTotpProperties(@NonNull TotpProperties totpProperties) {
        this.totpProperties = totpProperties;
    }
}
