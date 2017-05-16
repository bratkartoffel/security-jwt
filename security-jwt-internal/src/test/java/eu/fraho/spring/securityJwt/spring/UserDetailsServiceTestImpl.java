package eu.fraho.spring.securityJwt.spring;

import eu.fraho.spring.securityJwt.dto.JwtUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

@Service
public class UserDetailsServiceTestImpl implements UserDetailsService {
    public static final String BASE32_TOTP = "MZXW6YTBOI======";

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
        user.setAuthorities(Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));
        user.setApiAccessAllowed(true);

        if (username.equals("user_totp")) {
            user.setTotpSecret(Optional.of(BASE32_TOTP));
        }

        return user;
    }
}
