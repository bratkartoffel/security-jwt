/*
 * MIT Licence
 * Copyright (c) 2020 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.ut.dto;

import com.nimbusds.jwt.JWTClaimsSet;
import eu.fraho.spring.securityJwt.base.dto.JwtUser;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

public class JwtUserTest {
    public JwtUser newInstance() {
        JwtUser user = new JwtUser();
        user.setId(42L);
        user.setUsername("John Snow");
        user.setAuthorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_NIGHTWATCH")));
        return user;
    }

    @Test
    public void testToClaims() throws ParseException {
        JwtUser user = newInstance();
        JWTClaimsSet claims = user.toClaims().build();

        Assert.assertEquals("Wrong username in claim", user.getUsername(), claims.getSubject());
        Assert.assertEquals("Wrong id in claim", user.getId(), claims.getLongClaim("uid"));
        Assert.assertEquals("Wrong authorities in claim",
                user.getAuthorities().iterator().next().toString(),
                claims.getStringListClaim("authorities").iterator().next());
    }

    @Test
    public void testToClaimsMultipleAuthorities() throws ParseException {
        JwtUser user = newInstance();
        user.setAuthorities(Arrays.asList(
                new SimpleGrantedAuthority("ROLE_NIGHTWATCH"),
                new SimpleGrantedAuthority("HOUSE_STARK")
        ));
        JWTClaimsSet claims = user.toClaims().build();

        Assert.assertEquals("Wrong username in claim", user.getUsername(), claims.getSubject());
        Assert.assertEquals("Wrong id in claim", user.getId(), claims.getLongClaim("uid"));
        Assert.assertTrue("ROLE_NIGHTWATCH not present",
                user.getAuthorities().stream().map(GrantedAuthority::toString).anyMatch("ROLE_NIGHTWATCH"::equals));
        Assert.assertTrue("HOUSE_STARK not present",
                user.getAuthorities().stream().map(GrantedAuthority::toString).anyMatch("HOUSE_STARK"::equals));
    }

    @Test
    public void testToAndFromClaimsSymetric() throws ParseException {
        JwtUser user = newInstance();
        JWTClaimsSet claims1 = user.toClaims().build();
        JwtUser user2 = new JwtUser();
        user2.applyClaims(claims1);
        Assert.assertEquals("toClaims and fromClaims should work both ways", user, user2);
    }

    @Test
    public void testEraseCredentials() {
        JwtUser user = newInstance();
        user.setPassword("winteriscoming");
        user.setTotpSecret("foobar");
        user.eraseCredentials();

        Assert.assertNull("Password not cleared", user.getPassword());
        Assert.assertEquals("Totp secret not cleared", Optional.empty(), user.getTotpSecret());
    }

    @Test
    public void testToStringSensitive() {
        JwtUser user = newInstance();
        user.setPassword("winteriscoming");

        Assert.assertFalse("Password in toString present", user.toString().contains("winteriscoming"));
    }

    @Test(expected = ParseException.class)
    public void testInvalidUid() throws ParseException {
        JwtUser user = newInstance();

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("foobar")
                .claim("uid", "foobar")
                .claim("authorities", Collections.singletonList("foobar"))
                .build();
        user.applyClaims(claims);
    }

    @Test(expected = ParseException.class)
    public void testInvalidAuthorities() throws ParseException {
        JwtUser user = newInstance();

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("foobar")
                .claim("uid", 42L)
                .claim("authorities", "foobar")
                .build();
        user.applyClaims(claims);
    }

    @Test
    public void testToString() {
        JwtUser user = newInstance();
        Assert.assertFalse("toString contains password", user.toString().toLowerCase().contains("password"));
    }

    @Test
    public void testEquals() {
        JwtUser userA = newInstance();
        JwtUser userB = newInstance();
        Assert.assertEquals("Didn't equal", userA, userB);

        userA.setPassword("xxxx");
        Assert.assertEquals("Changed password should equal", userA, userB);

        userA.setUsername("xxxx");
        Assert.assertNotEquals("Changed username should not equals", userA, userB);
    }
}
