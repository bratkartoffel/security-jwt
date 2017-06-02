package eu.fraho.spring.securityJwt.dto;

import org.junit.Assert;
import org.junit.Test;

public class TestToken {
    @Test
    public void testAccessToken() {
        AccessToken a = new AccessToken("foobar", 9_999);
        AccessToken b = new AccessToken("foobar", 1_000);
        AccessToken c = new AccessToken("baz", 9_999);

        Assert.assertEquals("ExpiresIn should not matter in equal", a, b);
        Assert.assertNotEquals("Token should matter in equal", a, c);
        Assert.assertEquals("ExpiresIn should not matter in hashCode", a.hashCode(), b.hashCode());
        Assert.assertNotEquals("Token should matter in hashCode", a.hashCode(), c.hashCode());
    }

    @Test
    public void testRefreshToken() {
        RefreshToken a = new RefreshToken("foobar", 9_999, "none");
        RefreshToken b = new RefreshToken("foobar", 1_000, "none");
        RefreshToken c = new RefreshToken("foobar", 1_000, "baz");

        Assert.assertEquals("ExpiresIn should not matter in equal", a, b);
        Assert.assertNotEquals("DeviceID should matter in equal", a, c);
        Assert.assertEquals("ExpiresIn should not matter in hashCode", a.hashCode(), b.hashCode());
        Assert.assertNotEquals("DeviceID should matter in hashCode", a.hashCode(), c.hashCode());
    }
}
