/*
 * MIT Licence
 * Copyright (c) 2021 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.it.spring;

import eu.fraho.spring.securityJwt.base.dto.JwtUser;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class UserDetailsServiceTestImpl implements UserDetailsService {
    public static final String BASE32_TOTP = "MZXW6YTBOI======";

    private final AtomicBoolean apiAccessAllowed = new AtomicBoolean(true);
    private PasswordEncoder passwordEncoder;
    private ObjectFactory<JwtUser> jwtUser;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        JwtUser user = jwtUser.getObject();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(username));
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setEnabled(true);
        if ("admin".equals(username)) {
            user.setAuthorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        } else {
            user.setAuthorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        }
        user.setApiAccessAllowed(true);
        if ("noRefresh".equals(username)) {
            user.setApiAccessAllowed(apiAccessAllowed.get());
        }

        if ("user_totp".equals(username)) {
            user.setTotpSecret(BASE32_TOTP);
        }

        return user;
    }

    public void setApiAccessAllowed(boolean b) {
        apiAccessAllowed.set(b);
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder);
    }

    @Autowired
    public void setJwtUser(ObjectFactory<JwtUser> jwtUser) {
        this.jwtUser = Objects.requireNonNull(jwtUser);
    }
}
