package org.kosit.validator.client;

import jakarta.ws.rs.core.MediaType;

public class ValidationResponse<T> {

    private final T body;

    private final int statusCode;

    private final MediaType contentType;

    public ValidationResponse(T body, int statusCode, MediaType contentType) {
        this.body = body;
        this.statusCode = statusCode;
        this.contentType = contentType;
    }

    public T getBody() {
        return body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public MediaType getContentType() {
        return contentType;
    }

    public boolean isSuccess() {
        return statusCode >= 200 && statusCode < 300;
    }

    public T getBodyOrThrow(String message) {
        if (!isSuccess()) {
            throw new ValidatorClientException(message + " (status " + statusCode + ")", null);
        }
        return body;
    }
}
