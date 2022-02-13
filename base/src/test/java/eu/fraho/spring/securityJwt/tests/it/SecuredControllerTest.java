/*
 * MIT Licence
 * Copyright (c) 2022 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.tests.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.fraho.spring.securityJwt.base.dto.AccessToken;
import eu.fraho.spring.securityJwt.base.dto.AuthenticationResponse;
import eu.fraho.spring.securityJwt.base.it.spring.TestApiApplication;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest(classes = TestApiApplication.class)
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
public class SecuredControllerTest {
    public static final ObjectMapper mapper = new ObjectMapper();

    public static final String AUTH_LOGIN = "/auth/login";
    public static final String AUTH_REFRESH = "/auth/refresh";
    public static final String API_ADMIN = "/api/admin";
    public static final String API_USER = "/api/user";
    public static final String HELLO_WORLD = "Hello world!";

    private MockMvc mockMvc;

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
    public void testLoggedInRequestAdmin() throws Exception {
        AccessToken token = getTokenFor("admin").getAccessToken();
        MockHttpServletRequestBuilder req = MockMvcRequestBuilders.get(API_ADMIN)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", token.getType() + " " + token.getToken());

        mockMvc.perform(req)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(HELLO_WORLD));
    }

    private AuthenticationResponse getTokenFor(String username) throws Exception {
        MockHttpServletRequestBuilder req;
        String body;

        req = MockMvcRequestBuilders.post(AUTH_LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\",\"password\":\"" + username + "\"}")
                .accept(MediaType.APPLICATION_JSON);

        body = mockMvc.perform(req)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        AuthenticationResponse token = mapper.readValue(body, AuthenticationResponse.class);
        Assertions.assertNotNull(token.getAccessToken(), "Should return a token");
        return token;
    }

    @Autowired
    public void setMockMvc(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }
}
