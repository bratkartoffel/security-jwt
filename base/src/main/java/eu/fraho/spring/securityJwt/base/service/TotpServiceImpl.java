/*
 * MIT Licence
 * Copyright (c) 2020 Simon Frankenberger
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

    private int getCode(byte[] secret, long timeIndex) throws NoSuchAlgorithmException, InvalidKeyException {
        final SecretKeySpec signKey = new SecretKeySpec(secret, "HmacSHA1");
        final ByteBuffer buffer = ByteBuffer.allocate(8).putLong(timeIndex);
        final byte[] timeBytes = buffer.array();
        final byte[] hash = computeMac(signKey, timeBytes);
        final int offset = hash[19] & 0xf;
        long truncatedHash = hash[offset] & 0x7f;
        for (int i = 1; i < 4; i++) {
            truncatedHash <<= 8;
            truncatedHash |= hash[offset + i] & 0xff;
        }
        return (int) (truncatedHash % 1000000);
    }

    private byte[] computeMac(SecretKeySpec signKey, byte[] data) throws NoSuchAlgorithmException, InvalidKeyException {
        final Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(signKey);
        return mac.doFinal(data);
    }

    @Override
    public boolean verifyCode(String secret, int code) {
        final long timeIndex = System.currentTimeMillis() / 1000 / 30;
        final byte[] secretBytes = base32.decode(secret);
        if (secretBytes.length == 0) {
            return false;
        }

        boolean result = false;
        try {
            for (int i = -totpProperties.getVariance(); i <= totpProperties.getVariance(); i++) {
                int calculated = getCode(secretBytes, timeIndex + i);
                log.trace("Verifying code i={}, calculated={}, given={}", i, calculated, code);
                if (calculated == code) {
                    result = true;
                    break;
                }
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalArgumentException ike) {
            log.error("Error checking totp pin", ike);
        }
        return result;
    }

    @Override
    public String generateSecret() {
        final byte[] secret = new byte[totpProperties.getLength()];
        random.nextBytes(secret);
        log.debug("Generated secret with length=", secret.length);
        return base32.encodeToString(secret);
    }

    @Autowired
    public void setTotpProperties(@NonNull TotpProperties totpProperties) {
        this.totpProperties = totpProperties;
    }
}
