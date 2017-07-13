/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.config;

import eu.fraho.spring.securityJwt.JwtAuthenticationEntryPoint;
import eu.fraho.spring.securityJwt.JwtAuthenticationTokenFilter;
import eu.fraho.spring.securityJwt.service.JwtTokenService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @NonNull
    private final JwtAuthenticationEntryPoint unauthorizedHandler;

    @NonNull
    private final UserDetailsService userDetailsService;

    @NonNull
    private final PasswordEncoder passwordEncoder;

    @NonNull
    private final JwtTokenService jwtTokenUtil;

    @Autowired
    public void configureAuthentication(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        log.debug("Configuring AuthenticationManagerBuilder");
        authenticationManagerBuilder
                .userDetailsService(this.userDetailsService)
                .passwordEncoder(passwordEncoder);
    }

    @Bean
    public JwtAuthenticationTokenFilter authenticationTokenFilterBean() {
        log.debug("Creating JwtAuthenticationTokenFilter");
        return new JwtAuthenticationTokenFilter(jwtTokenUtil);
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        log.debug("Configuring HttpSecurity");

        httpSecurity
                // we don't need CSRF because our token is invulnerable
                .csrf().disable()
                // use our unauthorized handler
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler)
                .and()
                // don't create session
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                // Custom JWT based security filter
                .addFilterBefore(authenticationTokenFilterBean(), UsernamePasswordAuthenticationFilter.class);
    }
}
