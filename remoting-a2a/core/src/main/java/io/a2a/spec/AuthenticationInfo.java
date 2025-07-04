package io.a2a.spec;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.a2a.util.Assert;

/**
 * The authentication info for an agent.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public record AuthenticationInfo(List<String> schemes, String credentials) {

    public AuthenticationInfo {
        Assert.checkNotNullParam("schemes", schemes);
    }
}
