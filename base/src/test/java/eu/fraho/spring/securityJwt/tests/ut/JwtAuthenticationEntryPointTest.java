/*
 * MIT Licence
 * Copyright (c) 2020 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.tests.ut;

import eu.fraho.spring.securityJwt.base.JwtAuthenticationEntryPoint;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthenticationEntryPointTest {
    private JwtAuthenticationEntryPoint getNewInstance() {
        return new JwtAuthenticationEntryPoint();
    }

    @Test
    public void testSendError() throws IOException {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        JwtAuthenticationEntryPoint instance = getNewInstance();

        instance.commence(request, response, null);

        Mockito.verify(response).sendError(Mockito.eq(401), Mockito.eq("Unauthorized"));
    }
}
