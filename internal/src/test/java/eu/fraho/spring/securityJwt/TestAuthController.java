package eu.fraho.spring.securityJwt;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.fraho.spring.securityJwt.service.TotpServiceImpl;
import eu.fraho.spring.securityJwt.spring.TestApiApplication;
import eu.fraho.spring.securityJwt.spring.UserDetailsServiceTestImpl;
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
import java.util.Map;

@Getter
@Setter(AccessLevel.NONE)
@Slf4j
@SpringBootTest(properties = "spring.config.location=classpath:test-auth.yaml",
        classes = TestApiApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class TestAuthController extends AbstractTest {
    public static final ObjectMapper mapper = new ObjectMapper();
    public static final String AUTH_LOGIN = "/auth/login";
    public static final String AUTH_REFRESH = "/auth/refresh";
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private Filter springSecurityFilterChain;
    @Autowired
    private TotpServiceImpl totpService;
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
    public void testLoginSuccess() throws Exception {
        MockHttpServletRequestBuilder req;

        req = MockMvcRequestBuilders.post(AUTH_LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"user\",\"password\":\"user\"}")
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(req)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
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
    public void testRefresh() throws Exception {
        MockHttpServletRequestBuilder req;

        // success
        String token = getRefreshToken();
        req = MockMvcRequestBuilders.post(AUTH_REFRESH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"user\",\"refreshToken\":\"" + token + "\"}")
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(req)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        // check double usage
        mockMvc.perform(req)
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn();
    }

    @Test
    public void testRefreshWithWrongDeviceId() throws Exception {
        MockHttpServletRequestBuilder req;

        // success
        String token = getRefreshToken();
        req = MockMvcRequestBuilders.post(AUTH_REFRESH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"user\",\"refreshToken\":\"" + token + "\",\"deviceId\":\"foobar\"}")
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(req)
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn();
    }

    @Test
    public void testRefreshWithLongDeviceId() throws Exception {
        MockHttpServletRequestBuilder req;

        // success
        final String deviceId = "01234567891234567890";
        String token = getRefreshToken(deviceId);
        req = MockMvcRequestBuilders.post(AUTH_REFRESH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"user\",\"refreshToken\":\"" + token + "\",\"deviceId\":\"" + deviceId + "\"}")
                .accept(MediaType.APPLICATION_JSON);

        Assert.assertTrue("DeviceID not truncated", mockMvc.perform(req)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn().getResponse().getContentAsString().contains("\"0123456789\""));
    }

    @Test
    public void testRefreshWithEmptyDeviceId() throws Exception {
        MockHttpServletRequestBuilder req;

        // success
        final String deviceId = "";
        String token = getRefreshToken(deviceId);
        req = MockMvcRequestBuilders.post(AUTH_REFRESH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"user\",\"refreshToken\":\"" + token + "\",\"deviceId\":\"" + deviceId + "\"}")
                .accept(MediaType.APPLICATION_JSON);

        Assert.assertTrue("DeviceID not default", mockMvc.perform(req)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn().getResponse().getContentAsString().contains("\"__default\""));
    }

    @Test
    public void testRefreshWithCustomDeviceId() throws Exception {
        MockHttpServletRequestBuilder req;

        // success
        String token = getRefreshToken("foobar");
        req = MockMvcRequestBuilders.post(AUTH_REFRESH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"user\",\"refreshToken\":\"" + token + "\",\"deviceId\":\"foobar\"}")
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(req)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
    }

    @Test
    public void testRefreshWithMultipleDeviceIds() throws Exception {
        MockHttpServletRequestBuilder req;

        // success
        String token1 = getRefreshToken("baz");
        String token2 = getRefreshToken();

        // use first token
        req = MockMvcRequestBuilders.post(AUTH_REFRESH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"user\",\"refreshToken\":\"" + token1 + "\",\"deviceId\":\"baz\"}")
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(req)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        // use second token
        req = MockMvcRequestBuilders.post(AUTH_REFRESH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"user\",\"refreshToken\":\"" + token2 + "\"}")
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(req)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
    }

    @Test
    public void testRefreshWrongToken() throws Exception {
        MockHttpServletRequestBuilder req;

        req = MockMvcRequestBuilders.post(AUTH_REFRESH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"user\",\"refreshToken\":\"foobar\"}")
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(req)
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn();
    }

    @Test
    public void testRefreshUnknownUser() throws Exception {
        MockHttpServletRequestBuilder req;

        req = MockMvcRequestBuilders.post(AUTH_REFRESH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"userX\",\"refreshToken\":\"foobar\"}")
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(req)
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn();
    }

    private String getRefreshTokenInternal(String json) throws Exception {
        MockHttpServletRequestBuilder req = MockMvcRequestBuilders.post(AUTH_LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON);

        byte[] body = mockMvc.perform(req)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        return String.valueOf(((Map) mapper.readValue(body, Map.class).get("refreshToken")).get("token"));
    }

    private String getRefreshToken(String deviceId) throws Exception {
        return getRefreshTokenInternal("{\"username\":\"user\",\"password\":\"user\",\"deviceId\":\"" + deviceId + "\"}");
    }

    private String getRefreshToken() throws Exception {
        return getRefreshTokenInternal("{\"username\":\"user\",\"password\":\"user\"}");
    }
}
