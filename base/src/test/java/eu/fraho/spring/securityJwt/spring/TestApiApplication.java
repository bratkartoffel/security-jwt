package eu.fraho.spring.securityJwt.spring;

import eu.fraho.spring.securityJwt.AbstractTest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootApplication(scanBasePackages = {"eu.fraho.spring.securityJwt"})
@EnableSpringConfigured
@Slf4j
public class TestApiApplication {
    private static final String STATIC_SECRET = "this is not really a secret. nobody expects the spanish inquisition.";

    public static void main(String[] args) throws IOException {
        log.info("Running test application, creating static keyfile");
        AbstractTest.checkAndCreateOutDirs(AbstractTest.OUT_KEY);
        Files.write(Paths.get(AbstractTest.OUT_KEY), STATIC_SECRET.getBytes(StandardCharsets.UTF_8));

        log.info("Starting spring context");
        SpringApplication.run(TestApiApplication.class, args);

    }
}
