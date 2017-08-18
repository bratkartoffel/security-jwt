/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.spring;

import eu.fraho.spring.securityJwt.dto.JwtUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class UserDetailsServiceTestImpl implements UserDetailsService {
    public static final String BASE32_TOTP = "MZXW6YTBOI======";
    public final transient AtomicBoolean apiAccessAllowed = new AtomicBoolean(true);

    @Autowired
    private PasswordEncoder passwordEncoder = null;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        JwtUser user = new JwtUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(username));
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setEnabled(true);
        if (username.equals("admin")) {
            user.setAuthorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        } else {
            user.setAuthorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        }
        user.setApiAccessAllowed(apiAccessAllowed.get());

        if (username.equals("user_totp")) {
            user.setTotpSecret(BASE32_TOTP);
        }

        return user;
    }
}
