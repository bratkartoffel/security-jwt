/*
 * MIT Licence
 * Copyright (c) 2022 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.starter;

import eu.fraho.spring.securityJwt.base.config.RefreshCookieProperties;
import eu.fraho.spring.securityJwt.base.config.RefreshProperties;
import eu.fraho.spring.securityJwt.base.config.TokenCookieProperties;
import eu.fraho.spring.securityJwt.base.config.TokenHeaderProperties;
import eu.fraho.spring.securityJwt.base.config.TokenProperties;
import eu.fraho.spring.securityJwt.base.config.TotpProperties;
import eu.fraho.spring.securityJwt.base.controller.LoginRestController;
import eu.fraho.spring.securityJwt.base.controller.LogoutRestController;
import eu.fraho.spring.securityJwt.base.dto.JwtUser;
import eu.fraho.spring.securityJwt.base.service.JwtTokenService;
import eu.fraho.spring.securityJwt.base.service.JwtTokenServiceImpl;
import eu.fraho.spring.securityJwt.base.service.LoginService;
import eu.fraho.spring.securityJwt.base.service.LoginServiceImpl;
import eu.fraho.spring.securityJwt.base.service.RefreshService;
import eu.fraho.spring.securityJwt.base.service.RefreshServiceImpl;
import eu.fraho.spring.securityJwt.base.service.TotpService;
import eu.fraho.spring.securityJwt.base.service.TotpServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@AutoConfigureBefore(SecurityAutoConfiguration.class)
@EnableMethodSecurity(securedEnabled = true)
@Slf4j
public class SecurityJwtBaseAutoConfiguration {
    @Bean
    public TotpProperties totpProperties() {
        log.debug("Register TotpProperties");
        return new TotpProperties();
    }

    @Bean
    public TotpService totpService() {
        log.debug("Register TotpService");
        TotpServiceImpl totpService = new TotpServiceImpl();
        totpService.setTotpProperties(totpProperties());
        return totpService;
    }

    @Bean
    public TokenProperties tokenProperties() {
        log.debug("Register TokenProperties");
        return new TokenProperties();
    }

    @Bean
    public RefreshProperties refreshProperties() {
        log.debug("Register RefreshProperties");
        return new RefreshProperties();
    }

    @Bean
    public JwtTokenService jwtTokenService() {
        log.debug("Register JwtTokenService");
        JwtTokenServiceImpl jwtTokenService = new JwtTokenServiceImpl();
        jwtTokenService.setTokenProperties(tokenProperties());
        jwtTokenService.setRefreshProperties(refreshProperties());
        jwtTokenService.setTokenCookieProperties(tokenCookieProperties());
        jwtTokenService.setTokenHeaderProperties(tokenHeaderProperties());
        jwtTokenService.setRefreshCookieProperties(refreshCookieProperties());
        jwtTokenService.setJwtUser(this::jwtUser);
        return jwtTokenService;
    }

    @Bean
    public TokenCookieProperties tokenCookieProperties() {
        log.debug("Register TokenCookieProperties");
        return new TokenCookieProperties();
    }

    @Bean
    public TokenHeaderProperties tokenHeaderProperties() {
        log.debug("Register TokenHeaderProperties");
        return new TokenHeaderProperties();
    }

    @Bean
    public RefreshCookieProperties refreshCookieProperties() {
        log.debug("Register RefreshCookieProperties");
        return new RefreshCookieProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder passwordEncoder() {
        log.debug("Register BCryptPasswordEncoder");
        return new BCryptPasswordEncoder();
    }


    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    @ConditionalOnMissingBean
    public JwtUser jwtUser() {
        log.debug("Register JwtUser");
        return new JwtUser();
    }

    @Bean
    @ConditionalOnMissingBean
    public UserDetailsService defaultUserDetailsService() {
        log.debug("Register EmptyUserDetailsService");
        return new EmptyUserDetailsService();
    }

    @Bean
    @ConditionalOnMissingBean
    public LoginService loginService(final AuthenticationManager authenticationManager,
                                     final JwtTokenService jwtTokenService,
                                     final UserDetailsService userDetailsService,
                                     final TotpService totpService) {
        LoginServiceImpl service = new LoginServiceImpl();
        service.setAuthenticationManager(authenticationManager);
        service.setJwtTokenService(jwtTokenService);
        service.setUserDetailsService(userDetailsService);
        service.setTotpService(totpService);
        return service;
    }

    @Bean
    @ConditionalOnMissingBean
    public RefreshService refreshService(final JwtTokenService jwtTokenService) {
        RefreshServiceImpl service = new RefreshServiceImpl();
        service.setJwtTokenService(jwtTokenService);
        return service;
    }

    @Bean
    public LoginRestController loginRestController(final LoginService loginService,
                                                   final TokenCookieProperties tokenCookieProperties,
                                                   final RefreshCookieProperties refreshCookieProperties) {
        log.debug("Register LoginRestController");
        LoginRestController controller = new LoginRestController();
        controller.setLoginService(loginService);
        controller.setTokenCookieProperties(tokenCookieProperties);
        controller.setRefreshCookieProperties(refreshCookieProperties);
        return controller;
    }

    @Bean
    @ConditionalOnExpression("'${fraho.jwt.token.cookie.enabled}' == 'true' or '${fraho.jwt.refresh.cookie.enabled}' == 'true'")
    public LogoutRestController logoutRestController(final TokenCookieProperties tokenCookieProperties,
                                                     final RefreshCookieProperties refreshCookieProperties) {
        log.debug("Register LogoutRestController");
        LogoutRestController controller = new LogoutRestController();
        controller.setTokenCookieProperties(tokenCookieProperties);
        controller.setRefreshCookieProperties(refreshCookieProperties);
        return controller;
    }
}
