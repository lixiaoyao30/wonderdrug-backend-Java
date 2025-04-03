package org.wonder.wonderdrugs.exception;

public class VaultApiException extends RuntimeException {
    private String errorType;
    private int statusCode;

    public VaultApiException(String message) {
        super(message);
    }

    public VaultApiException(String message, String errorType, int statusCode) {
        super(message);
        this.errorType = errorType;
        this.statusCode = statusCode;
    }

    public String getErrorType() {
        return errorType;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
