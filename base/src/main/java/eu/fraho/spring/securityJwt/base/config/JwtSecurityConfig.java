/*
 * MIT Licence
 * Copyright (c) 2026 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.config;

import eu.fraho.spring.securityJwt.base.JwtAuthenticationTokenFilter;
import eu.fraho.spring.securityJwt.base.service.JwtTokenService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.lang.reflect.Constructor;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
@Order(90)
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class JwtSecurityConfig {
    private UserDetailsService userDetailsService;
    private PasswordEncoder passwordEncoder;
    private JwtTokenService jwtTokenService;

    @Bean
    public AuthenticationProvider authenticationProvider() {
        log.debug("Configuring AuthenticationManagerBuilder");
        try {
            // try default constructor [3.0.0 - 4.0.0[
            DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
            authProvider.setUserDetailsService(userDetailsService);
            authProvider.setPasswordEncoder(passwordEncoder);
            return authProvider;
        } catch (NoSuchMethodError nsme) {
            try {
                // try constructor with UserDetailsService [4.0.0
                Constructor<DaoAuthenticationProvider> constructor = DaoAuthenticationProvider.class.getConstructor(UserDetailsService.class);
                DaoAuthenticationProvider authProvider = constructor.newInstance(userDetailsService);
                authProvider.setPasswordEncoder(passwordEncoder);
                return authProvider;
            } catch (ReflectiveOperationException roe2) {
                roe2.addSuppressed(nsme);
                throw new BeanInitializationException("Could not create DaoAuthenticationProvider", nsme);
            }
        }
    }

    @Bean
    public JwtAuthenticationTokenFilter authenticationTokenFilterBean() {
        log.debug("Creating JwtAuthenticationTokenFilter");
        JwtAuthenticationTokenFilter filter = new JwtAuthenticationTokenFilter();
        filter.setJwtTokenService(jwtTokenService);
        return filter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        log.info("Loading fraho security-jwt version {}", JwtSecurityConfig.class.getPackage().getImplementationVersion());
        log.debug("Configuring HttpSecurity");

        httpSecurity
                .authenticationProvider(authenticationProvider())
                // we don't need CSRF because our token is invulnerable
                .csrf(AbstractHttpConfigurer::disable)
                // use our unauthorized handler
                .exceptionHandling(httpSecurityExceptionHandlingConfigurer -> httpSecurityExceptionHandlingConfigurer.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                // don't create session
                .sessionManagement(httpSecuritySessionManagementConfigurer -> httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Custom JWT based security filter
                .addFilterBefore(authenticationTokenFilterBean(), UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfiguration) throws
            Exception {
        return authConfiguration.getAuthenticationManager();
    }

    @Autowired
    @NonNull
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Autowired
    @NonNull
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    @NonNull
    public void setJwtTokenService(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }
}
