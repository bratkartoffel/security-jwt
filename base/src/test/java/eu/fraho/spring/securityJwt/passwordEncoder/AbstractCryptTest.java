package eu.fraho.spring.securityJwt.passwordEncoder;

import eu.fraho.spring.securityJwt.AbstractTest;
import eu.fraho.spring.securityJwt.CryptPasswordEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
abstract class AbstractCryptTest extends AbstractTest {
    @Autowired
    protected CryptPasswordEncoder cryptPasswordEncoder = null;
}
