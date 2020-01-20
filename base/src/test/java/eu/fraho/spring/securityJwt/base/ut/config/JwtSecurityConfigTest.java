/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.ut.config;

import eu.fraho.spring.securityJwt.base.JwtAuthenticationEntryPoint;
import eu.fraho.spring.securityJwt.base.JwtAuthenticationTokenFilter;
import eu.fraho.spring.securityJwt.base.config.JwtSecurityConfig;
import eu.fraho.spring.securityJwt.base.service.JwtTokenService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.userdetails.DaoAuthenticationConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RunWith(PowerMockRunner.class)
@PrepareForTest({HttpSecurity.class, CsrfConfigurer.class, ExceptionHandlingConfigurer.class, SessionManagementConfigurer.class})
public class JwtSecurityConfigTest {
    private JwtSecurityConfig getNewInstance() {
        return getNewInstance(Mockito.mock(JwtAuthenticationEntryPoint.class));
    }

    private JwtSecurityConfig getNewInstance(JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        UserDetailsService userDetailsService = Mockito.mock(UserDetailsService.class);
        PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);
        JwtTokenService tokenService = Mockito.mock(JwtTokenService.class);
        JwtSecurityConfig config = new JwtSecurityConfig();
        config.setUserDetailsService(userDetailsService);
        config.setPasswordEncoder(passwordEncoder);
        config.setJwtTokenService(tokenService);
        config.setJwtAuthenticationEntryPoint(jwtAuthenticationEntryPoint);
        return config;
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testConfigureAuthentication() throws Exception {
        DaoAuthenticationConfigurer daoAuthenticationConfigurer = Mockito.mock(DaoAuthenticationConfigurer.class);
        AuthenticationManagerBuilder authenticationManagerBuilder = Mockito.mock(AuthenticationManagerBuilder.class);
        Mockito.when(authenticationManagerBuilder.userDetailsService(Mockito.any())).thenReturn(daoAuthenticationConfigurer);

        getNewInstance().configure(authenticationManagerBuilder);

        Mockito.verify(authenticationManagerBuilder).userDetailsService(Mockito.any());
        Mockito.verify(daoAuthenticationConfigurer).passwordEncoder(Mockito.any(PasswordEncoder.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testConfigure() throws Exception {
        JwtAuthenticationEntryPoint authenticationEntryPoint = Mockito.mock(JwtAuthenticationEntryPoint.class);
        HttpSecurity httpSecurity = PowerMockito.mock(HttpSecurity.class);
        CsrfConfigurer csrfConfigurer = PowerMockito.mock(CsrfConfigurer.class);
        ExceptionHandlingConfigurer exceptionHandlingConfigurer = PowerMockito.mock(ExceptionHandlingConfigurer.class);
        SessionManagementConfigurer sessionManagementConfigurer = PowerMockito.mock(SessionManagementConfigurer.class);

        PowerMockito.when(httpSecurity.csrf()).thenReturn(csrfConfigurer);
        PowerMockito.when(csrfConfigurer.disable()).thenReturn(httpSecurity);
        PowerMockito.when(httpSecurity.exceptionHandling()).thenReturn(exceptionHandlingConfigurer);
        PowerMockito.when(exceptionHandlingConfigurer.authenticationEntryPoint(Mockito.any())).thenReturn(exceptionHandlingConfigurer);
        PowerMockito.when(exceptionHandlingConfigurer.and()).thenReturn(httpSecurity);
        PowerMockito.when(httpSecurity.sessionManagement()).thenReturn(sessionManagementConfigurer);
        PowerMockito.when(sessionManagementConfigurer.sessionCreationPolicy(Mockito.any())).thenReturn(sessionManagementConfigurer);
        PowerMockito.when(sessionManagementConfigurer.and()).thenReturn(httpSecurity);

        getNewInstance(authenticationEntryPoint).configure(httpSecurity);

        Mockito.verify(csrfConfigurer).disable();
        Mockito.verify(exceptionHandlingConfigurer).authenticationEntryPoint(authenticationEntryPoint);
        Mockito.verify(sessionManagementConfigurer).sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        Mockito.verify(httpSecurity).addFilterBefore(Mockito.any(JwtAuthenticationTokenFilter.class), Mockito.eq(UsernamePasswordAuthenticationFilter.class));
    }
}
