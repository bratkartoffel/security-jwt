/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.starter;

import eu.fraho.spring.securityJwt.CryptPasswordEncoder;
import eu.fraho.spring.securityJwt.JwtAuthenticationEntryPoint;
import eu.fraho.spring.securityJwt.JwtAuthenticationTokenFilter;
import eu.fraho.spring.securityJwt.config.*;
import eu.fraho.spring.securityJwt.controller.AuthenticationRestController;
import eu.fraho.spring.securityJwt.service.JwtTokenService;
import eu.fraho.spring.securityJwt.service.JwtTokenServiceImpl;
import eu.fraho.spring.securityJwt.service.TotpService;
import eu.fraho.spring.securityJwt.service.TotpServiceImpl;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.Security;

@Configuration
@AutoConfigureBefore(SecurityAutoConfiguration.class)
public class SecurityJwtBaseAutoConfiguration {
    @Bean
    public JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter() {
        return new JwtAuthenticationTokenFilter(jwtTokenService());
    }

    @Bean
    public TotpConfiguration totpConfig() {
        return new TotpConfiguration();
    }

    @Bean
    public CryptConfiguration cryptConfiguration() {
        return new CryptConfiguration();
    }

    @Bean
    public TotpService totpService() {
        return new TotpServiceImpl(totpConfig());
    }

    @Bean
    public JwtTokenConfiguration jwtTokenConfiguration() {
        return new JwtTokenConfiguration();
    }

    @Bean
    public JwtRefreshConfiguration jwtRefreshConfiguration() {
        return new JwtRefreshConfiguration();
    }

    @Bean
    public JwtTokenService jwtTokenService() {
        return new JwtTokenServiceImpl(jwtTokenConfiguration(), jwtRefreshConfiguration());
    }

    @Bean
    public JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint() {
        return new JwtAuthenticationEntryPoint();
    }

    @Bean
    @ConditionalOnClass(BouncyCastleProvider.class)
    public BouncyCastleProvider bouncyCastleProvider() {
        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.removeProvider(provider.getName());
        Security.addProvider(provider);
        return provider;
    }

    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder passwordEncoder() {
        return new CryptPasswordEncoder(cryptConfiguration());
    }

    @Bean
    @ConditionalOnMissingBean
    public UserDetailsService defaultUserDetailsService() {
        return new EmptyUserDetailsService();
    }

    @Bean
    @ConditionalOnWebApplication
    public AuthenticationRestController authenticationRestController(final AuthenticationManager authenticationManager,
                                                                     final JwtTokenService jwtTokenService,
                                                                     final UserDetailsService userDetailsService,
                                                                     final TotpService totpService) {
        return new AuthenticationRestController(authenticationManager, jwtTokenService, userDetailsService, totpService);
    }

    @Bean
    @ConditionalOnWebApplication
    public WebSecurityConfig webSecurityConfig(final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                                               final UserDetailsService userDetailsService,
                                               final PasswordEncoder passwordEncoder,
                                               final JwtTokenService jwtTokenService) {
        return new WebSecurityConfig(jwtAuthenticationEntryPoint, userDetailsService, passwordEncoder, jwtTokenService);
    }
}
