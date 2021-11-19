/*
 * MIT Licence
 * Copyright (c) 2021 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.tests.it;

import eu.fraho.spring.securityJwt.base.config.RefreshCookieProperties;
import eu.fraho.spring.securityJwt.base.it.spring.TestApiApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;
import java.util.Objects;

@SpringBootTest(properties = "spring.config.location=classpath:test-cookies.yaml", classes = TestApiApplication.class)
@ExtendWith(SpringExtension.class)
public class AuthControllerWithCookiesTest {
    public static final String AUTH_LOGIN = "/auth/login";
    public static final String AUTH_LOGOUT = "/auth/logout";

    private WebApplicationContext webApplicationContext;

    private Filter springSecurityFilterChain;

    private RefreshCookieProperties refreshCookieProperties;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        synchronized (this) {
            if (mockMvc == null) {
                mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .addFilter(springSecurityFilterChain).build();
            }
        }
    }

    @Test
    public void testLoginSuccess() throws Exception {
        MockHttpServletRequestBuilder req;

        req = MockMvcRequestBuilders.post(AUTH_LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"user\",\"password\":\"user\"}")
                .accept(MediaType.APPLICATION_JSON);

        getMockMvc().perform(req)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.cookie().exists("access"))
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    public void testLogout() throws Exception {
        MockHttpServletRequestBuilder req;

        req = MockMvcRequestBuilders.post(AUTH_LOGOUT);
        getMockMvc().perform(req)
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andExpect(MockMvcResultMatchers.cookie().exists("access"))
                .andExpect(MockMvcResultMatchers.cookie().exists("refresh"))
                .andExpect(MockMvcResultMatchers.content().string(""));
    }

    public Filter getSpringSecurityFilterChain() {
        return this.springSecurityFilterChain;
    }

    @Autowired
    public void setSpringSecurityFilterChain(Filter springSecurityFilterChain) {
        this.springSecurityFilterChain = Objects.requireNonNull(springSecurityFilterChain);
    }

    public RefreshCookieProperties getRefreshCookieProperties() {
        return this.refreshCookieProperties;
    }

    @Autowired
    public void setRefreshCookieProperties(RefreshCookieProperties refreshCookieProperties) {
        this.refreshCookieProperties = Objects.requireNonNull(refreshCookieProperties);
    }

    public WebApplicationContext getWebApplicationContext() {
        return this.webApplicationContext;
    }

    @Autowired
    public void setWebApplicationContext(WebApplicationContext webApplicationContext) {
        this.webApplicationContext = Objects.requireNonNull(webApplicationContext);
    }

    public MockMvc getMockMvc() {
        return this.mockMvc;
    }
}
