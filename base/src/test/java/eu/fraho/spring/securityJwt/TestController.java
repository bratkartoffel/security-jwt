/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.fraho.spring.securityJwt.dto.AccessToken;
import eu.fraho.spring.securityJwt.dto.JwtAuthenticationResponse;
import eu.fraho.spring.securityJwt.spring.TestApiApplication;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
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
import java.io.IOException;

@Getter
@Setter(AccessLevel.NONE)
@Slf4j
@SpringBootTest(properties = "spring.config.location=classpath:test-auth.yaml",
        classes = TestApiApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class TestController extends AbstractTest {
    public static final ObjectMapper mapper = new ObjectMapper();
    public static final String AUTH_LOGIN = "/auth/login";
    public static final String AUTH_REFRESH = "/auth/refresh";
    public static final String API_ADMIN = "/api/admin";
    public static final String API_USER = "/api/user";
    public static final String HELLO_WORLD = "Hello world!";
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private Filter springSecurityFilterChain;
    private MockMvc mockMvc;

    @BeforeClass
    public static void beforeClass() throws IOException {
        AbstractTest.beforeHmacClass();

        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.findAndRegisterModules();

        System.setProperty("IN_TESTING", "1");
    }

    @Before
    public void setup() {
        if (mockMvc == null) {
            mockMvc = MockMvcBuilders.webAppContextSetup(context).addFilter(springSecurityFilterChain).build();
        }
    }

    @Test
    public void testLoggedInRequestWrongRole() throws Exception {
        AccessToken token = getTokenFor("user").getAccessToken();
        MockHttpServletRequestBuilder req = MockMvcRequestBuilders.get(API_ADMIN)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", token.getType() + " " + token.getToken());

        mockMvc.perform(req)
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    public void testRequestNoToken() throws Exception {
        MockHttpServletRequestBuilder req = MockMvcRequestBuilders.get(API_ADMIN)
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(req)
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    public void testRequestEmptyToken() throws Exception {
        MockHttpServletRequestBuilder req = MockMvcRequestBuilders.get(API_ADMIN)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "");

        mockMvc.perform(req)
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    public void testRequestInvalidToken() throws Exception {
        MockHttpServletRequestBuilder req = MockMvcRequestBuilders.get(API_ADMIN)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "foobar");

        mockMvc.perform(req)
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    public void testLoggedInRequestUser() throws Exception {
        AccessToken token = getTokenFor("user").getAccessToken();
        MockHttpServletRequestBuilder req = MockMvcRequestBuilders.get(API_USER)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", token.getType() + " " + token.getToken());

        mockMvc.perform(req)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(HELLO_WORLD));
    }

    @Test
    public void testLoggedInRefresh() throws Exception {
        JwtAuthenticationResponse tokens1 = getTokenFor("user");

        Assert.assertNotNull("Should return a token", tokens1.getAccessToken().getToken());
        Assert.assertNotNull("Should return a refresh token", tokens1.getRefreshToken().getToken());


        MockHttpServletRequestBuilder req;
        String body;

        req = MockMvcRequestBuilders.post(AUTH_REFRESH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"user\",\"refreshToken\":\"" + tokens1.getRefreshToken().getToken()
                        + "\",\"deviceId\":\"" + tokens1.getRefreshToken().getDeviceId() + "\"}")
                .accept(MediaType.APPLICATION_JSON);

        body = mockMvc.perform(req)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn().getResponse().getContentAsString();

        JwtAuthenticationResponse tokens2 = mapper.readValue(body, JwtAuthenticationResponse.class);

        Assert.assertNotNull("Should return a token", tokens2.getAccessToken().getToken());
        Assert.assertNotEquals("Should return a new token", tokens1.getAccessToken().getToken(), tokens2.getAccessToken().getToken());
        Assert.assertNotNull("Should return a refresh token", tokens2.getRefreshToken().getToken());
        Assert.assertNotEquals("Should return a new refresh token", tokens1.getRefreshToken().getToken(), tokens2.getRefreshToken().getToken());

    }

    @Test
    public void testLoggedInRequestAdmin() throws Exception {
        AccessToken token = getTokenFor("admin").getAccessToken();
        MockHttpServletRequestBuilder req = MockMvcRequestBuilders.get(API_ADMIN)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", token.getType() + " " + token.getToken());

        mockMvc.perform(req)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(HELLO_WORLD));
    }

    private JwtAuthenticationResponse getTokenFor(String username) throws Exception {
        MockHttpServletRequestBuilder req;
        String body;

        req = MockMvcRequestBuilders.post(AUTH_LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\",\"password\":\"" + username + "\"}")
                .accept(MediaType.APPLICATION_JSON);

        body = mockMvc.perform(req)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn().getResponse().getContentAsString();

        JwtAuthenticationResponse token = mapper.readValue(body, JwtAuthenticationResponse.class);
        Assert.assertNotNull("Should return a token", token.getAccessToken());
        Assert.assertNotNull("Should return a refresh token", token.getRefreshToken());
        return token;
    }
}
