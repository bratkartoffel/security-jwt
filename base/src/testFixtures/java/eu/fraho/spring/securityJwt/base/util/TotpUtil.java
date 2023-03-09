/*
 * MIT Licence
 * Copyright (c) 2022 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.util;

import eu.fraho.spring.securityJwt.base.service.TotpService;
import eu.fraho.spring.securityJwt.base.service.TotpServiceImpl;
import org.apache.commons.codec.binary.Base32;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class TotpUtil {
    public static int getCodeForTesting(TotpService service, String secret, int varianceDiff) {
        Base32 base32 = new Base32();
        byte[] decoded = base32.decode(secret);
        long timeIndex = (System.currentTimeMillis() / 1000 / 30) + varianceDiff;
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(0, timeIndex);

        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(decoded, "HmacSHA1"));
            Method method = TotpServiceImpl.class.getDeclaredMethod("createCode", Mac.class, byte[].class);
            method.setAccessible(true);
            Object result = method.invoke(service, mac, buffer.array());
            return (Integer) result;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | NoSuchAlgorithmException |
                 InvalidKeyException e) {
            throw new IllegalStateException(e);
        }
    }
}
