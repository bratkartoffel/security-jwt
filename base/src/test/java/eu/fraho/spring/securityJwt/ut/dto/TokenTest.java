/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.ut.dto;

import eu.fraho.spring.securityJwt.dto.AccessToken;
import eu.fraho.spring.securityJwt.dto.RefreshToken;
import org.junit.Assert;
import org.junit.Test;

public class TokenTest {
    @Test
    public void testAccessToken() {
        AccessToken a = new AccessToken("foobar", 9_999);
        AccessToken b = new AccessToken("foobar", 1_000);
        AccessToken c = new AccessToken("baz", 9_999);

        Assert.assertEquals("ExpiresIn should not matter in equal", a, b);
        Assert.assertNotEquals("AbstractToken should matter in equal", a, c);
        Assert.assertEquals("ExpiresIn should not matter in hashCode", a.hashCode(), b.hashCode());
        Assert.assertNotEquals("AbstractToken should matter in hashCode", a.hashCode(), c.hashCode());
    }

    @Test
    public void testRefreshToken() {
        RefreshToken a = new RefreshToken("foobar", 9_999);
        RefreshToken b = new RefreshToken("foobar", 1_000);

        Assert.assertEquals("ExpiresIn should not matter in equal", a, b);
        Assert.assertEquals("ExpiresIn should not matter in hashCode", a.hashCode(), b.hashCode());
    }
}
