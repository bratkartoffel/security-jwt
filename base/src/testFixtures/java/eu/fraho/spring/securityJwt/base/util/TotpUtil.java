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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TotpUtil {
    public static int getCodeForTesting(TotpService service, String secret, int varianceDiff) {
        Base32 base32 = new Base32();
        byte[] decoded = base32.decode(secret);
        long timeIndex = (System.currentTimeMillis() / 1000 / 30) + varianceDiff;

        try {
            Method method = TotpServiceImpl.class.getDeclaredMethod("getCode", byte[].class, long.class);
            method.setAccessible(true);
            Object result = method.invoke(service, decoded, timeIndex);
            return (Integer) result;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }
}
