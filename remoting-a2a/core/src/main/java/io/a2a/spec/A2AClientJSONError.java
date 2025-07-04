package io.a2a.spec;

public class A2AClientJSONError extends A2AClientError {

    public A2AClientJSONError() {
    }

    public A2AClientJSONError(String message) {
        super(message);
    }

    public A2AClientJSONError(String message, Throwable cause) {
        super(message, cause);
    }
}
