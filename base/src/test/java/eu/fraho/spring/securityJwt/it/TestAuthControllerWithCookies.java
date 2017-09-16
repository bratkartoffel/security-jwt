/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.it;

import eu.fraho.spring.securityJwt.config.JwtRefreshCookieConfiguration;
import eu.fraho.spring.securityJwt.it.spring.TestApiApplication;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
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
import javax.servlet.http.Cookie;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Slf4j
@SpringBootTest(properties = "spring.config.location=classpath:test-cookies.yaml",
        classes = TestApiApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class TestAuthControllerWithCookies {
    public static final String AUTH_LOGIN = "/auth/login";

    public static final String AUTH_REFRESH = "/auth/refresh";

    public static final String AUTH_REFRESH_COOKIES = "/auth/refreshCookie";

    @Autowired
    @Getter
    private WebApplicationContext webApplicationContext;

    @Autowired
    private Filter springSecurityFilterChain;

    @Autowired
    private JwtRefreshCookieConfiguration refreshCookieConfiguration;

    @Getter
    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
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

        String body = getMockMvc().perform(req)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(MockMvcResultMatchers.cookie().exists("access"))
                .andExpect(MockMvcResultMatchers.cookie().exists("refresh"))
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue("Refresh token was not generated", body.contains("\"refreshToken\":{\""));
    }

    @Test
    public void testRefreshWithCookieNoReferer() throws Exception {
        MockHttpServletRequestBuilder req;
        Map<String, Cookie> cookies = getCookies();

        req = MockMvcRequestBuilders.get(AUTH_REFRESH_COOKIES)
                .cookie(cookies.get("refresh"));

        String body = getMockMvc().perform(req)
                .andExpect(MockMvcResultMatchers.status().isSeeOther())
                .andExpect(MockMvcResultMatchers.header().string("Location", "/"))
                .andExpect(MockMvcResultMatchers.cookie().exists("access"))
                .andExpect(MockMvcResultMatchers.cookie().exists("refresh"))
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue("Body was generated", body.isEmpty());
    }

    @Test
    public void testRefreshWithCookieReferer() throws Exception {
        MockHttpServletRequestBuilder req;
        Map<String, Cookie> cookies = getCookies();

        req = MockMvcRequestBuilders.get(AUTH_REFRESH_COOKIES)
                .header("Referer", "/foobar")
                .cookie(cookies.get("refresh"));

        String body = getMockMvc().perform(req)
                .andExpect(MockMvcResultMatchers.status().isTemporaryRedirect())
                .andExpect(MockMvcResultMatchers.header().string("Location", "/foobar"))
                .andExpect(MockMvcResultMatchers.cookie().exists("access"))
                .andExpect(MockMvcResultMatchers.cookie().exists("refresh"))
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue("Body was generated", body.isEmpty());
    }

    @Test
    public void testRefreshNoCookie() throws Exception {
        MockHttpServletRequestBuilder req;

        req = MockMvcRequestBuilders.get(AUTH_REFRESH_COOKIES);

        String body = getMockMvc().perform(req)
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.cookie().doesNotExist("access"))
                .andExpect(MockMvcResultMatchers.cookie().doesNotExist("refresh"))
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue("Body was generated", body.isEmpty());
    }

    @Test
    public void testRefreshInvalidCookie() throws Exception {
        MockHttpServletRequestBuilder req;
        Map<String, Cookie> cookies = getCookies();

        Cookie cookie = cookies.get("refresh");
        cookie.setValue("foobar");
        req = MockMvcRequestBuilders.get(AUTH_REFRESH_COOKIES)
                .cookie(cookie);

        String body = getMockMvc().perform(req)
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.cookie().doesNotExist("access"))
                .andExpect(MockMvcResultMatchers.cookie().doesNotExist("refresh"))
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue("Body was generated", body.isEmpty());
    }

    @Test
    public void testRefreshWhenDisabled() throws Exception {
        MockHttpServletRequestBuilder req;
        req = MockMvcRequestBuilders.get(AUTH_REFRESH_COOKIES);

        try {
            refreshCookieConfiguration.setEnabled(false);
            String body = getMockMvc().perform(req)
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.cookie().doesNotExist("access"))
                    .andExpect(MockMvcResultMatchers.cookie().doesNotExist("refresh"))
                    .andReturn().getResponse().getContentAsString();
            Assert.assertTrue("Body was generated", body.isEmpty());
        } finally {
            refreshCookieConfiguration.setEnabled(true);
        }
    }

    /**
     * name = access | refresh, see config
     *
     * @return name -> cookie
     * @throws Exception If the request fails
     */
    private Map<String, Cookie> getCookies() throws Exception {
        MockHttpServletRequestBuilder req = MockMvcRequestBuilders.post(AUTH_LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"user\",\"password\":\"user\"}")
                .accept(MediaType.APPLICATION_JSON);

        return Arrays.stream(getMockMvc().perform(req)
                .andReturn()
                .getResponse()
                .getCookies()).collect(Collectors.toMap(Cookie::getName, c -> c));
    }
}
