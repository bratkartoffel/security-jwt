package eu.fraho.spring.securityJwt.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.jcip.annotations.Immutable;

@Value
@Immutable
@EqualsAndHashCode(callSuper = true)
public class RefreshToken extends Token {
    private final String deviceId;

    @JsonCreator
    public RefreshToken(@JsonProperty("token") String token, @JsonProperty("expiresIn") int expiresIn, @JsonProperty("deviceId") String deviceId) {
        super(token, expiresIn);
        this.deviceId = deviceId;
    }
}
