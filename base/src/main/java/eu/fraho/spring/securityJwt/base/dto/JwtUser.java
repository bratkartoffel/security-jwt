/*
 * MIT Licence
 * Copyright (c) 2020 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nimbusds.jwt.JWTClaimsSet;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Data
@EqualsAndHashCode(of = {"id", "username"})
@ToString(of = {"id", "username", "apiAccessAllowed", "authorities"})
@NoArgsConstructor
@Slf4j
public class JwtUser implements UserDetails, CredentialsContainer {
    @Schema(description = "Identifier")
    private Long id = -1L;

    @Schema(description = "Username")
    private String username = "anonymousUser";

    @JsonIgnore
    @Schema(description = "Password")
    private String password = null;

    @JsonIgnore
    @Schema(description = "TOTP secret")
    private String totpSecret = null;

    @Schema(description = "Account is not expired?")
    private boolean accountNonExpired = false;

    @Schema(description = "Account is not locked?")
    private boolean accountNonLocked = false;

    @Schema(description = "Credentials are not expired?")
    private boolean credentialsNonExpired = false;

    @Schema(description = "Account is enabled?")
    private boolean enabled = false;

    @JsonIgnore
    @Schema(description = "List of authorities")
    private List<GrantedAuthority> authorities = new ArrayList<>();

    @Schema(description = "API access is allowed?")
    private boolean apiAccessAllowed = false;

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

    public void applyClaims(JWTClaimsSet claims) throws ParseException {
        setUsername(claims.getSubject());
        setId(claims.getLongClaim("uid"));
        Optional.ofNullable(claims.getStringListClaim("authorities"))
                .ifPresent(a -> setAuthorities(a.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList())));
    }

    public Optional<String> getTotpSecret() {
        return Optional.ofNullable(totpSecret);
    }

    public void setTotpSecret(String totpSecret) {
        this.totpSecret = totpSecret;
    }
}
