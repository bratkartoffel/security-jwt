package eu.fraho.spring.securityJwt;

import eu.fraho.spring.securityJwt.dto.CryptAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.Crypt;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Random;

/**
 * This service provides a {@link PasswordEncoder} to generate and validate Unix-Crypt compatible passwords.
 * The generated passwords use the configured algorithm and (if supported) count of rounds.
 * <h3>Used properties from configuration file:</h3>
 * <table border="1">
 * <tr>
 * <th>Property</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>fraho.crypt.rounds</td>
 * <td>{@value #ROUNDS_DEFAULT}</td>
 * <td>Defines the &quot;strength&quot; of the hashing function. The more rounds used, the more secure the generated hash.
 * But beware that more rounds mean more cpu-load and longer computation times!</td>
 * </tr>
 * <tr>
 * <td>fraho.crypt.algorithm</td>
 * <td>{@value #ALGORITHM_DEFAULT}</td>
 * <td>Configured the used crypt algorithm. For a list of possible values see {@link CryptAlgorithm}</td>
 * </tr>
 * </table>
 *
 * @see #encode(CharSequence)
 * @see #matches(CharSequence, String)
 */
@Component
@Slf4j
public class CryptPasswordEncoder implements PasswordEncoder, InitializingBean {
    public static final String ALGORITHM_DEFAULT = "SHA512";

    public static final int ROUNDS_MIN = 100;
    public static final int ROUNDS_DEFAULT = 10_000;
    public static final int ROUNDS_MAX = 500_000;

    private final Random random = new SecureRandom();
    private final Encoder encoder = Base64.getUrlEncoder();
    @Value("${fraho.crypt.rounds:" + ROUNDS_DEFAULT + "}")
    private int rounds = ROUNDS_DEFAULT;
    @Value("${fraho.crypt.algorithm:" + ALGORITHM_DEFAULT + "}")
    private CryptAlgorithm algorithm = CryptAlgorithm.valueOf(ALGORITHM_DEFAULT);

    private static boolean slowEquals(CharSequence a, CharSequence b) {
        int diff = a.length() ^ b.length();
        for (int i = 0; i < a.length() && i < b.length(); i++) {
            diff |= a.charAt(i) ^ b.charAt(i);
        }
        return diff == 0;
    }

    @Override
    public String encode(CharSequence rawPassword) {
        final String cryptParam;
        if (algorithm.isRoundsSupported()) {
            cryptParam = String.format("%srounds=%d$%s$", algorithm.getPrefix(), rounds, generateSalt());
        } else if (CryptAlgorithm.DES.equals(algorithm)) {
            cryptParam = generateSalt();
        } else {
            cryptParam = String.format("%s%s$", algorithm.getPrefix(), generateSalt());
        }

        log.debug("Using crypt params: {}", cryptParam);
        return Crypt.crypt(rawPassword.toString(), cryptParam);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return slowEquals(encodedPassword, Crypt.crypt(rawPassword.toString(), encodedPassword));
    }

    protected String generateSalt() {
        final byte[] bytes = new byte[algorithm.getSaltLength() * 2];
        random.nextBytes(bytes);
        String salt = encoder.encodeToString(bytes);
        salt = salt.replaceAll("[-_]", "");
        return salt.substring(0, algorithm.getSaltLength());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (rounds < ROUNDS_MIN || rounds > ROUNDS_MAX) {
            log.warn("Encryption rounds out of bounds ({} <= {} <= {}), forcing to default ({})",
                    ROUNDS_MIN, rounds, ROUNDS_MAX, ROUNDS_DEFAULT);
            rounds = ROUNDS_DEFAULT;
        }

        if (algorithm.isInsecure()) {
            log.warn("Using insecure crypt variant {}. Consider upgrading to a stronger one.", algorithm);
        }
    }
}
