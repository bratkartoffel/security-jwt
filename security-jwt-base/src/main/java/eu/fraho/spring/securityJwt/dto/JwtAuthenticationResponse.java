package eu.fraho.spring.securityJwt.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;
import net.jcip.annotations.Immutable;

@Immutable
@Value
@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class JwtAuthenticationResponse {
    private AccessToken accessToken;
    private RefreshToken refreshToken;
}
