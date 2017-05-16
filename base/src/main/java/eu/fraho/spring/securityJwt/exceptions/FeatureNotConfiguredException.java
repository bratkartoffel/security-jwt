package eu.fraho.spring.securityJwt.exceptions;

public class FeatureNotConfiguredException extends JwtSecurityException {
    public FeatureNotConfiguredException(String message) {
        super(message);
    }
}
