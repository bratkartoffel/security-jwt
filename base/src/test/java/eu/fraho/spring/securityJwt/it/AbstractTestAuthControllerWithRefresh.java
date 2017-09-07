/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.fraho.spring.securityJwt.it.spring.UserDetailsServiceTestImpl;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Map;

@SuppressWarnings("unused")
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public abstract class AbstractTestAuthControllerWithRefresh extends AbstractTestAuthController {
    @NonNull
    private final UserDetailsServiceTestImpl userDetailsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

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
                .andReturn();
    }

    @Test
    public void testRefresh() throws Exception {
        MockHttpServletRequestBuilder req;

        String token = getRefreshToken();
        req = MockMvcRequestBuilders.post(AUTH_REFRESH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + token + "\"}")
                .accept(MediaType.APPLICATION_JSON);

        getMockMvc().perform(req)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        // check double usage
        getMockMvc().perform(req)
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn();
    }

    @Test
    public void testRefreshDisabledAccount() throws Exception {
        MockHttpServletRequestBuilder req;

        final String token = getRefreshTokenU("noRefresh");

        req = MockMvcRequestBuilders.post(AUTH_REFRESH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + token + "\"}")
                .accept(MediaType.APPLICATION_JSON);

        getMockMvc().perform(req)
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn();
    }

    @Test
    public void testMultipleRefreshTokens() throws Exception {
        MockHttpServletRequestBuilder req;

        String token1 = getRefreshToken();
        String token2 = getRefreshToken();

        // use first token
        req = MockMvcRequestBuilders.post(AUTH_REFRESH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + token1 + "\"}")
                .accept(MediaType.APPLICATION_JSON);

        getMockMvc().perform(req)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        // use second token
        req = MockMvcRequestBuilders.post(AUTH_REFRESH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + token2 + "\"}")
                .accept(MediaType.APPLICATION_JSON);

        getMockMvc().perform(req)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
    }

    @Test
    public void testRefreshWrongToken() throws Exception {
        MockHttpServletRequestBuilder req;

        req = MockMvcRequestBuilders.post(AUTH_REFRESH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"foobar\"}")
                .accept(MediaType.APPLICATION_JSON);

        getMockMvc().perform(req)
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn();
    }

    @Test
    public void testRefreshUnknownUser() throws Exception {
        MockHttpServletRequestBuilder req;

        req = MockMvcRequestBuilders.post(AUTH_REFRESH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"foobar\"}")
                .accept(MediaType.APPLICATION_JSON);

        getMockMvc().perform(req)
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn();
    }

    private String getRefreshTokenInternal(String json) throws Exception {
        MockHttpServletRequestBuilder req = MockMvcRequestBuilders.post(AUTH_LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON);

        byte[] body = getMockMvc().perform(req)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        return String.valueOf(((Map) objectMapper.readValue(body, Map.class).get("refreshToken")).get("token"));
    }

    protected String getRefreshToken() throws Exception {
        return getRefreshTokenU("user");
    }

    protected String getRefreshTokenU(String username) throws Exception {
        return getRefreshTokenInternal("{\"username\":\"" + username + "\",\"password\":\"" + username + "\"}");
    }
}
