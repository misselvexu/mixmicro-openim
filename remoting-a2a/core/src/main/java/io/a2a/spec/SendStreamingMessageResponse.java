package io.a2a.spec;

import static io.a2a.spec.A2A.JSONRPC_VERSION;
import static io.a2a.util.Utils.defaultIfNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.a2a.util.Assert;

/**
 * The response after receiving a request to initiate a task with streaming.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class SendStreamingMessageResponse extends JSONRPCResponse<StreamingEventKind> {

    @JsonCreator
    public SendStreamingMessageResponse(@JsonProperty("jsonrpc") String jsonrpc, @JsonProperty("id") Object id,
                                        @JsonProperty("result") StreamingEventKind result, @JsonProperty("error") JSONRPCError error) {
        this.jsonrpc = defaultIfNull(jsonrpc, JSONRPC_VERSION);
        Assert.isNullOrStringOrInteger(id);
        this.id = id;
        this.result = result;
        this.error = error;
    }

    public SendStreamingMessageResponse(Object id, StreamingEventKind result) {
        this(null, id, result, null);
    }

    public SendStreamingMessageResponse(Object id, JSONRPCError error) {
        this(null, id, null, error);
    }
}
