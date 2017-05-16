package eu.fraho.spring.securityJwt.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.jcip.annotations.Immutable;

@Value
@Immutable
@EqualsAndHashCode(callSuper = true)
public class AccessToken extends Token {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String type = "Bearer";

    @JsonCreator
    public AccessToken(@JsonProperty("token") String token, @JsonProperty("expiresIn") int expiresIn) {
        super(token, expiresIn);
    }
}
