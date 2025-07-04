package io.a2a.spec;

import static io.a2a.spec.APIKeySecurityScheme.API_KEY;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = APIKeySecurityScheme.class, name = API_KEY),
        @JsonSubTypes.Type(value = HTTPAuthSecurityScheme.class, name = HTTPAuthSecurityScheme.HTTP),
        @JsonSubTypes.Type(value = OAuth2SecurityScheme.class, name = OAuth2SecurityScheme.OAUTH2),
        @JsonSubTypes.Type(value = OpenIdConnectSecurityScheme.class, name = OpenIdConnectSecurityScheme.OPENID_CONNECT)
})
public sealed interface SecurityScheme permits APIKeySecurityScheme, HTTPAuthSecurityScheme, OAuth2SecurityScheme, OpenIdConnectSecurityScheme {

    String getDescription();
}
