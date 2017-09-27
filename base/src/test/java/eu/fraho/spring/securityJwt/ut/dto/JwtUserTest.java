/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.ut.dto;

import com.nimbusds.jwt.JWTClaimsSet;
import eu.fraho.spring.securityJwt.dto.JwtUser;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JWTClaimsSet.class})
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

    @Test
    public void testNoUid() throws ParseException {
        JwtUser user = newInstance();

        JWTClaimsSet claims = PowerMockito.mock(JWTClaimsSet.class);
        Mockito.when(claims.getSubject()).thenReturn("foobar");
        Mockito.doThrow(new ParseException("foobar", 0)).when(claims).getLongClaim("uid");
        Mockito.doThrow(new ParseException("foobar", 0)).when(claims).getStringListClaim("authorities");

        user.applyClaims(claims);

        Assert.assertEquals("UserID changed", Long.valueOf(42L), user.getId());
        Assert.assertEquals("Authorities changed", 1, user.getAuthorities().size());
        Assert.assertEquals("Wrong username parsed", "foobar", user.getUsername());
    }
}
