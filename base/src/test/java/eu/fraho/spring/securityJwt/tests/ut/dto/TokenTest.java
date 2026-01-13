/*
 * MIT Licence
 * Copyright (c) 2026 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.tests.ut.dto;

import eu.fraho.spring.securityJwt.base.dto.AccessToken;
import eu.fraho.spring.securityJwt.base.dto.RefreshToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TokenTest {
    @Test
    public void testAccessToken() {
        AccessToken a = new AccessToken("foobar", 9_999);
        AccessToken b = new AccessToken("foobar", 1_000);
        AccessToken c = new AccessToken("baz", 9_999);

        Assertions.assertEquals(a, b, "ExpiresIn should not matter in equal");
        Assertions.assertNotEquals(a, c, "AbstractToken should matter in equal");
        Assertions.assertEquals(a.hashCode(), b.hashCode(), "ExpiresIn should not matter in hashCode");
        Assertions.assertNotEquals(a.hashCode(), c.hashCode(), "AbstractToken should matter in hashCode");
    }

    @Test
    public void testRefreshToken() {
        RefreshToken a = new RefreshToken("foobar", 9_999);
        RefreshToken b = new RefreshToken("foobar", 1_000);

        Assertions.assertEquals(a, b, "ExpiresIn should not matter in equal");
        Assertions.assertEquals(a.hashCode(), b.hashCode(), "ExpiresIn should not matter in hashCode");
    }
}
