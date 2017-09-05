/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.*;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@EqualsAndHashCode(of = {"id", "username"})
@ToString(of = {"id", "username", "apiAccessAllowed", "authorities"})
@NoArgsConstructor
public class JwtUser implements UserDetails, CredentialsContainer {
    private Long id = -1L;

    private String username = "anonymousUser";

    @JsonIgnore
    private String password = null;

    @JsonIgnore
    private String totpSecret = null;

    private boolean accountNonExpired = false;

    private boolean accountNonLocked = false;

    private boolean credentialsNonExpired = false;

    private boolean enabled = false;

    @JsonIgnore
    private List<GrantedAuthority> authorities = new ArrayList<>();

    private boolean apiAccessAllowed = false;

    @SuppressWarnings("unchecked")
    public static JwtUser fromClaims(JWTClaimsSet claims) {
        JwtUser user = new JwtUser();
        user.setUsername(claims.getSubject());
        final List<String> claimAuthorities = (List<String>) claims.getClaim("authorities");
        final List<GrantedAuthority> newAuthorities = new ArrayList<>();
        for (String authority : claimAuthorities) {
            newAuthorities.add(new SimpleGrantedAuthority(authority));
        }
        user.setAuthorities(newAuthorities);
        user.setId(Long.valueOf(String.valueOf(claims.getClaim("uid"))));
        return user;
    }

    @Override
    public void eraseCredentials() {
        password = null;
        totpSecret = null;
    }

    public JWTClaimsSet.Builder toClaims() {
        final ArrayList<String> authorities = new ArrayList<>();
        for (GrantedAuthority authority : getAuthorities()) {
            authorities.add(authority.toString());
        }

        return new JWTClaimsSet.Builder()
                .subject(getUsername())
                .claim("uid", getId())
                .claim("authorities", authorities);
    }

    public Optional<String> getTotpSecret() {
        return Optional.ofNullable(totpSecret);
    }

    public void setTotpSecret(String totpSecret) {
        this.totpSecret = totpSecret;
    }
}
