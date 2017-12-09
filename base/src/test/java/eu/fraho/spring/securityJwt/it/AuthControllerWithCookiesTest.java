/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.it;

import eu.fraho.spring.securityJwt.config.RefreshCookieProperties;
import eu.fraho.spring.securityJwt.it.spring.TestApiApplication;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;

@Getter
@Slf4j
@SpringBootTest(properties = "spring.config.location=classpath:test-cookies.yaml",
        classes = TestApiApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class AuthControllerWithCookiesTest {
    public static final String AUTH_LOGIN = "/auth/login";

    @Setter(onMethod = @__({@Autowired, @NonNull}))
    @Getter
    private WebApplicationContext webApplicationContext;

    @Setter(onMethod = @__({@Autowired, @NonNull}))
    private Filter springSecurityFilterChain;

    @Setter(onMethod = @__({@Autowired, @NonNull}))
    private RefreshCookieProperties refreshCookieProperties;

    @Getter
    private MockMvc mockMvc;

    @Before
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
    public void testLoginSuccess() throws Exception {
        MockHttpServletRequestBuilder req;

        req = MockMvcRequestBuilders.post(AUTH_LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"user\",\"password\":\"user\"}")
                .accept(MediaType.APPLICATION_JSON);

        getMockMvc().perform(req)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(MockMvcResultMatchers.cookie().exists("access"))
                .andReturn().getResponse().getContentAsString();
    }
}
