/*
 * MIT Licence
 * Copyright (c) 2025 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.tests.ut.dto;

import com.nimbusds.jwt.JWTClaimsSet;
import eu.fraho.spring.securityJwt.base.dto.JwtUser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
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

        Assertions.assertEquals(user.getUsername(), claims.getSubject(), "Wrong username in claim");
        Assertions.assertEquals(user.getId(), claims.getLongClaim("uid"), "Wrong id in claim");
        Assertions.assertEquals(user.getAuthorities().iterator().next().toString(),
                claims.getStringListClaim("authorities").iterator().next(), "Wrong authorities in claim");
    }

    @Test
    public void testToClaimsMultipleAuthorities() throws ParseException {
        JwtUser user = newInstance();
        user.setAuthorities(Arrays.asList(
                new SimpleGrantedAuthority("ROLE_NIGHTWATCH"),
                new SimpleGrantedAuthority("HOUSE_STARK")
        ));
        JWTClaimsSet claims = user.toClaims().build();

        Assertions.assertEquals(user.getUsername(), claims.getSubject(), "Wrong username in claim");
        Assertions.assertEquals(user.getId(), claims.getLongClaim("uid"), "Wrong id in claim");
        Assertions.assertTrue(user.getAuthorities().stream().map(GrantedAuthority::toString).anyMatch("ROLE_NIGHTWATCH"::equals), "ROLE_NIGHTWATCH not present");
        Assertions.assertTrue(user.getAuthorities().stream().map(GrantedAuthority::toString).anyMatch("HOUSE_STARK"::equals), "HOUSE_STARK not present");
    }

    @Test
    public void testToAndFromClaimsSymetric() throws ParseException {
        JwtUser user = newInstance();
        JWTClaimsSet claims1 = user.toClaims().build();
        JwtUser user2 = new JwtUser();
        user2.applyClaims(claims1);
        Assertions.assertEquals(user, user2, "toClaims and fromClaims should work both ways");
    }

    @Test
    public void testEraseCredentials() {
        JwtUser user = newInstance();
        user.setPassword("winteriscoming");
        user.setTotpSecret("foobar");
        user.eraseCredentials();

        Assertions.assertNull(user.getPassword(), "Password not cleared");
        Assertions.assertEquals(Optional.empty(), user.getTotpSecret(), "Totp secret not cleared");
    }

    @Test
    public void testToStringSensitive() {
        JwtUser user = newInstance();
        user.setPassword("winteriscoming");

        Assertions.assertFalse(user.toString().contains("winteriscoming"), "Password in toString present");
    }

    @Test
    public void testInvalidUid() {
        JwtUser user = newInstance();

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("foobar")
                .claim("uid", "foobar")
                .claim("authorities", Collections.singletonList("foobar"))
                .build();
        Assertions.assertThrows(ParseException.class, () -> user.applyClaims(claims));
    }

    @Test
    public void testInvalidAuthorities() {
        JwtUser user = newInstance();

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("foobar")
                .claim("uid", 42L)
                .claim("authorities", "foobar")
                .build();
        Assertions.assertThrows(ParseException.class, () -> user.applyClaims(claims));
    }

    @Test
    public void testToString() {
        JwtUser user = newInstance();
        Assertions.assertFalse(user.toString().toLowerCase().contains("password"), "toString contains password");
    }

    @Test
    public void testEquals() {
        JwtUser userA = newInstance();
        JwtUser userB = newInstance();
        Assertions.assertEquals(userA, userB, "Didn't equal");

        userA.setPassword("xxxx");
        Assertions.assertEquals(userA, userB, "Changed password should equal");

        userA.setUsername("xxxx");
        Assertions.assertNotEquals(userA, userB, "Changed username should not equals");
    }
}
