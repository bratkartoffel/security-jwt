package eu.fraho.spring.securityJwt.exceptions;

public class JwtRefreshException extends JwtSecurityException {
    public JwtRefreshException(String message) {
        super(message);
    }

    public JwtRefreshException(String message, Throwable cause) {
        super(message, cause);
    }
}
