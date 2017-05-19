package eu.fraho.spring.securityJwt.spring;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;

import java.security.Security;

@SpringBootApplication(scanBasePackages = "eu.fraho.spring.securityJwt")
@EnableSpringConfigured
public class TestApiApplication {
    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());
        SpringApplication.run(TestApiApplication.class, args);
    }
}
