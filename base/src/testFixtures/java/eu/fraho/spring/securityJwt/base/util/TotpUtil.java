/*
 * MIT Licence
 * Copyright (c) 2026 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.util;

import eu.fraho.libs.totp.Totp;
import eu.fraho.libs.totp.TotpSettings;
import org.apache.commons.codec.binary.Base32;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class TotpUtil {
    public static int getCodeForTesting(String secret, int varianceDiff) {
        Totp totp = new Totp(TotpSettings.DEFAULT);
        Base32 base32 = new Base32();
        byte[] decoded = base32.decode(secret);
        long timeIndex = (System.currentTimeMillis() / 1000 / 30) + varianceDiff;
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(0, timeIndex);

        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(decoded, "HmacSHA1"));
            Method method = Totp.class.getDeclaredMethod("createCode", Mac.class, byte[].class, int.class);
            method.setAccessible(true);
            Object result = method.invoke(totp, mac, buffer.array(), 1_000_000);
            return (Integer) result;
        } catch (ReflectiveOperationException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException(e);
        }
    }
}
