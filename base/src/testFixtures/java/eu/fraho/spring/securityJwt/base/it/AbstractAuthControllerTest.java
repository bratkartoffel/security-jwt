/*
 * MIT Licence
 * Copyright (c) 2021 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.it;

import eu.fraho.spring.securityJwt.base.it.spring.UserDetailsServiceTestImpl;
import eu.fraho.spring.securityJwt.base.service.TotpServiceImpl;
import eu.fraho.spring.securityJwt.base.util.TotpUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;
import java.util.Objects;

abstract class AbstractAuthControllerTest {
    public static final String AUTH_LOGIN = "/auth/login";

    public static final String AUTH_REFRESH = "/auth/refresh";

    private WebApplicationContext webApplicationContext;

    private Filter springSecurityFilterChain;

    private TotpServiceImpl totpService;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        if (mockMvc == null) {
            synchronized (this) {
                if (mockMvc == null) {
                    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                            .addFilter(springSecurityFilterChain).build();
                }
            }
        }
    }

    @Test
    public void testLoginWrongPassword() throws Exception {
        MockHttpServletRequestBuilder req;

        req = MockMvcRequestBuilders.post(AUTH_LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"user\",\"password\":\"foobar\"}")
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(req)
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn();
    }

    @Test
    public void testLoginMissingTotp() throws Exception {
        MockHttpServletRequestBuilder req;

        req = MockMvcRequestBuilders.post(AUTH_LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"user_totp\",\"password\":\"user_totp\"}")
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(req)
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn();
    }

    @Test
    public void testLoginWrongTotp() throws Exception {
        MockHttpServletRequestBuilder req;

        req = MockMvcRequestBuilders.post(AUTH_LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"user_totp\",\"password\":\"user_totp\",\"totp\":123456}")
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(req)
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn();

    }

    @Test
    public void testLoginCorrectTotp() throws Exception {
        MockHttpServletRequestBuilder req;

        long totp = TotpUtil.getCodeForTesting(totpService, UserDetailsServiceTestImpl.BASE32_TOTP, 0);
        req = MockMvcRequestBuilders.post(AUTH_LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"user_totp\",\"password\":\"user_totp\",\"totp\":" + totp + "}")
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(req)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();
    }

    @Test
    public abstract void testRefresh() throws Exception;

    @Test
    public abstract void testLoginSuccess() throws Exception;

    @Autowired
    public void setSpringSecurityFilterChain(Filter springSecurityFilterChain) {
        this.springSecurityFilterChain = Objects.requireNonNull(springSecurityFilterChain);
    }

    public WebApplicationContext getWebApplicationContext() {
        return this.webApplicationContext;
    }

    @Autowired
    public void setWebApplicationContext(WebApplicationContext webApplicationContext) {
        this.webApplicationContext = Objects.requireNonNull(webApplicationContext);
    }

    public TotpServiceImpl getTotpService() {
        return this.totpService;
    }

    @Autowired
    public void setTotpService(TotpServiceImpl totpService) {
        this.totpService = Objects.requireNonNull(totpService);
    }

    public MockMvc getMockMvc() {
        return this.mockMvc;
    }
}
