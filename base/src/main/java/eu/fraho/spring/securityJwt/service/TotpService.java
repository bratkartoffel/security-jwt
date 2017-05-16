package eu.fraho.spring.securityJwt.service;

/**
 * This service provides integration of timed one-time-pads (TOTP). This includes the
 * generation of secrets and validation of codes against a secret.
 * <h3>Used properties from configuration file:</h3>
 * <table border="1" summary="list of configuration properties">
 * <tr>
 * <th>Property</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>fraho.totp.variance</td>
 * <td>{@value #TOTP_VARIANCE_DEFAULT}</td>
 * <td>Defines the allowed variance / validity of totp pins. The number defines how many
 * &quot;old / expired&quot; pins will be considered valid. A value of &quot;3&quot; is the official suggestion for TOPT.
 * This value is used to consider small clock-differences between client and server.</td>
 * </tr>
 * <tr>
 * <td>fraho.totp.length</td>
 * <td>{@value #TOTP_LENGTH_DEFAULT}</td>
 * <td>Defines the length of the generated totp secrets.</td>
 * </tr>
 * </table>
 *
 * @see #verifyCode(String, int)
 * @see #generateSecret()
 */
public interface TotpService {
    int TOTP_LENGTH_MIN = 8;
    int TOTP_LENGTH_DEFAULT = 16;
    int TOTP_LENGTH_MAX = 32;

    int TOTP_VARIANCE_MIN = 1;
    int TOTP_VARIANCE_DEFAULT = 3;
    int TOTP_VARIANCE_MAX = 10;

    /**
     * Verify the given code against the stored secret.
     *
     * @param secret The shared secret between client and server
     * @param code   The code to verify
     * @return {@code true} if the given code is within the configured variance bounds.
     * @throws NullPointerException if secret is null
     */
    boolean verifyCode(String secret, int code);

    /**
     * Generate a new shared secret.
     *
     * @return A base32-encoded secret for the client
     */
    String generateSecret();
}
