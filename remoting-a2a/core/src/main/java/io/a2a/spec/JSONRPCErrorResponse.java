package io.a2a.spec;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.a2a.util.Assert;

/**
 * A JSON RPC error response.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class JSONRPCErrorResponse extends JSONRPCResponse<Void> {

    @JsonCreator
    public JSONRPCErrorResponse(@JsonProperty("jsonrpc") String jsonrpc, @JsonProperty("id") Object id,
                                @JsonProperty("result") Void result, @JsonProperty("error") JSONRPCError error) {
        super(jsonrpc, id, result, error);
        Assert.checkNotNullParam("error", error);
    }

    public JSONRPCErrorResponse(Object id, JSONRPCError error) {
        this(null, id, null, error);
    }

    public JSONRPCErrorResponse(JSONRPCError error) {
        this(null, null, null, error);
    }
}
