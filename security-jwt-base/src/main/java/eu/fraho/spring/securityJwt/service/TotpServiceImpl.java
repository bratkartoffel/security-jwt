package eu.fraho.spring.securityJwt.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base32;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
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
public class TotpServiceImpl implements TotpService, InitializingBean {
    private final Base32 base32 = new Base32();
    private final Random random = new SecureRandom();

    @Value("${fraho.totp.variance:" + TOTP_VARIANCE_DEFAULT + "}")
    private Integer kmsTotpVariance = TOTP_VARIANCE_DEFAULT;

    @Value("${fraho.totp.length:" + TOTP_LENGTH_DEFAULT + "}")
    private Integer kmsTotpLength = TOTP_LENGTH_DEFAULT;

    public long getCurrentCodeForTesting(String secret) throws InvalidKeyException, NoSuchAlgorithmException {
        if (System.getProperty("IN_TESTING") == null) {
            throw new IllegalStateException("Not in testing mode!");
        }

        return getCode(base32.decode(secret), System.currentTimeMillis() / 1000 / 30);
    }

    private long getCode(byte[] secret, long timeIndex) throws NoSuchAlgorithmException, InvalidKeyException {
        final SecretKeySpec signKey = new SecretKeySpec(secret, "HmacSHA1");
        final ByteBuffer buffer = ByteBuffer.allocate(8).putLong(timeIndex);
        final byte[] timeBytes = buffer.array();

        final Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(signKey);
        final byte[] hash = mac.doFinal(timeBytes);

        final int offset = hash[19] & 0xf;
        long truncatedHash = hash[offset] & 0x7f;
        for (int i = 1; i < 4; i++) {
            truncatedHash <<= 8;
            truncatedHash |= hash[offset + i] & 0xff;
        }
        return truncatedHash % 1000000;
    }

    @Override
    public boolean verifyCode(String secret, int code) {
        final long timeIndex = System.currentTimeMillis() / 1000 / 30;
        final byte[] secretBytes = base32.decode(secret);
        boolean result = false;
        try {
            for (int i = -kmsTotpVariance; i <= kmsTotpVariance; i++) {
                if (getCode(secretBytes, timeIndex + i) == code) {
                    result = true;
                    break;
                }
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException ike) {
            log.error("Error checking totp pin", ike);
        }
        return result;
    }

    @Override
    public String generateSecret() {
        final byte[] secret = new byte[kmsTotpLength];
        random.nextBytes(secret);
        return base32.encodeToString(secret);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (kmsTotpVariance < TOTP_VARIANCE_MIN || kmsTotpVariance > TOTP_VARIANCE_MAX) {
            log.warn("TOTP variance out of bounds ({} <= {} <= {}), forcing to default ({})",
                    TOTP_VARIANCE_MIN, kmsTotpVariance, TOTP_VARIANCE_MAX, TOTP_VARIANCE_DEFAULT);
            kmsTotpVariance = TOTP_VARIANCE_DEFAULT;
        }
        if (kmsTotpLength < TOTP_LENGTH_MIN || kmsTotpLength > TOTP_LENGTH_MAX) {
            log.warn("TOTP length out of bounds ({} <= {} <= {}), forcing to default ({})",
                    TOTP_LENGTH_MIN, kmsTotpLength, TOTP_LENGTH_MAX, TOTP_LENGTH_DEFAULT);
            kmsTotpLength = TOTP_LENGTH_DEFAULT;
        }
    }
}
