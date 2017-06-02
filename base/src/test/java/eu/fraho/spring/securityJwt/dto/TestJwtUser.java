package eu.fraho.spring.securityJwt.dto;

import org.junit.Assert;
import org.junit.Test;

public class TestJwtUser {
    @Test
    public void testAccessToken() {
        JwtUser a = new JwtUser();
        a.setId(13L);
        a.setUsername("foobar");

        JwtUser b = new JwtUser();
        b.setId(13L);
        b.setUsername("foobar");
        b.setAuthority("asdfg");
        b.setAccountNonExpired(!a.isAccountNonExpired());

        JwtUser c = new JwtUser();
        c.setId(9_999L);
        c.setUsername("foobar");

        JwtUser d = new JwtUser();
        d.setId(13L);
        d.setUsername("baz");

        Assert.assertEquals("Only id and username should matter for equals", a, b);
        Assert.assertEquals("Only id and username should matter for hashCode", a.hashCode(), b.hashCode());

        Assert.assertNotEquals("Id should matter for equals", a, c);
        Assert.assertNotEquals("Id should matter for hashCode", a.hashCode(), c.hashCode());

        Assert.assertNotEquals("Username should matter for equals", a, d);
        Assert.assertNotEquals("Username should matter for hashCode", a.hashCode(), d.hashCode());
    }
}
