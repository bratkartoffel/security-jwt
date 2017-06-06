package eu.fraho.spring.securityJwt;

import eu.fraho.spring.securityJwt.exceptions.JwtRefreshException;
import org.junit.Test;

public class TestJwtRefreshExceptionForCoveragesSake {
    @Test(expected = JwtRefreshException.class)
    public void test() {
        throw new JwtRefreshException("Everything for the coverage!!!", new Exception());
    }
}
