/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.service;

import eu.fraho.spring.securityJwt.dto.RefreshToken;
import eu.fraho.spring.securityJwt.dto.TimeWithPeriod;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;
import java.util.Map;

public interface RefreshTokenStore extends InitializingBean {
    void saveToken(String username, String deviceId, String token);

    boolean useToken(String username, String deviceId, String token);

    List<RefreshToken> listTokens(String username);

    Map<String, List<RefreshToken>> listTokens();

    boolean revokeToken(String username, RefreshToken token);

    boolean revokeToken(String username, String deviceId);

    int revokeTokens(String username);

    int revokeTokens();

    TimeWithPeriod getRefreshExpiration();

    /*  Try to achieve some time constant compare  */
    default boolean tokenEquals(byte[] a, byte[] b) {
        int diff = a.length ^ b.length;
        for (int i = 0; i < a.length && i < b.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }

}
