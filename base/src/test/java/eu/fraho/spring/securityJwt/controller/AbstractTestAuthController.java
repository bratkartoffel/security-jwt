package eu.fraho.spring.securityJwt.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.fraho.spring.securityJwt.AbstractTest;
import eu.fraho.spring.securityJwt.service.TotpServiceImpl;
import eu.fraho.spring.securityJwt.spring.UserDetailsServiceTestImpl;
import lombok.Getter;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;
import java.io.IOException;

@Getter
public abstract class AbstractTestAuthController {
    public static final ObjectMapper mapper = new ObjectMapper();
    public static final String AUTH_LOGIN = "/auth/login";
    public static final String AUTH_REFRESH = "/auth/refresh";

    @Autowired
    protected WebApplicationContext context;

    @Autowired
    protected Filter springSecurityFilterChain;

    @Autowired
    protected TotpServiceImpl totpService;
    protected MockMvc mockMvc;

    public static void beforeClass() throws IOException {
        AbstractTest.beforeHmacClass();

        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.findAndRegisterModules();
    }

    @Before
    public void setup() {
        if (mockMvc == null) {
            mockMvc = MockMvcBuilders.webAppContextSetup(context).addFilter(springSecurityFilterChain).build();
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

        long totp = totpService.getCurrentCodeForTesting(UserDetailsServiceTestImpl.BASE32_TOTP);
        req = MockMvcRequestBuilders.post(AUTH_LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"user_totp\",\"password\":\"user_totp\",\"totp\":" + totp + "}")
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(req)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
    }

    @Test
    public abstract void testRefresh() throws Exception;

    @Test
    public abstract void testLoginSuccess() throws Exception;
}
