/*
 * MIT Licence
 * Copyright (c) 2026 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.it;

import eu.fraho.spring.securityJwt.base.it.spring.UserDetailsServiceTestImpl;
import eu.fraho.spring.securityJwt.base.service.TotpServiceImpl;
import eu.fraho.spring.securityJwt.base.util.TotpUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Objects;

@AutoConfigureMockMvc
abstract class AbstractAuthControllerTest {
    public static final String AUTH_LOGIN = "/auth/login";
    public static final String AUTH_REFRESH = "/auth/refresh";

    private TotpServiceImpl totpService;
    private MockMvc mockMvc;

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

        long totp = TotpUtil.getCodeForTesting(UserDetailsServiceTestImpl.BASE32_TOTP, 0);
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
    public void setTotpService(TotpServiceImpl totpService) {
        this.totpService = Objects.requireNonNull(totpService);
    }

    public MockMvc getMockMvc() {
        return this.mockMvc;
    }

    @Autowired
    public void setMockMvc(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }
}
