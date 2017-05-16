package eu.fraho.spring.securityJwt.service;

import eu.fraho.spring.securityJwt.AbstractTest;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class JwtTokenServiceImplAccessor extends AbstractTest {
    @Autowired
    protected JwtTokenServiceImpl jwtTokenService = null;

    protected Class<? extends RefreshTokenStore> getTokenStoreImplementation() {
        return jwtTokenService.getInternalRefreshTokenStoreType();
    }
}
