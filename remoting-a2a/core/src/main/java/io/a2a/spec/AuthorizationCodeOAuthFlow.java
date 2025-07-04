package io.a2a.spec;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.a2a.util.Assert;

/**
 * Configuration for the OAuth Authorization Code flow.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public record AuthorizationCodeOAuthFlow(String authorizationUrl, String refreshUrl, Map<String, String> scopes,
                                         String tokenUrl) {

    public AuthorizationCodeOAuthFlow {
        Assert.checkNotNullParam("authorizationUrl", authorizationUrl);
        Assert.checkNotNullParam("scopes", scopes);
        Assert.checkNotNullParam("tokenUrl", tokenUrl);
    }
}
