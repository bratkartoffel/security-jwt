package eu.fraho.spring.securityJwt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;
import net.jcip.annotations.Immutable;

import javax.validation.constraints.NotNull;
import java.util.Optional;

@Immutable
@Value
@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class JwtAuthenticationRequest {
    @NotNull
    @JsonProperty(required = true)
    private String username;
    @NotNull
    @JsonProperty(required = true)
    private String password;
    private Optional<String> deviceId;
    private Optional<Integer> totp;
}
