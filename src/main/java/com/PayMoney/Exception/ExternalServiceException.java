package com.PayMoney.Exception;

public class ExternalServiceException extends RuntimeException {

    public ExternalServiceException(
            String message,
            Throwable cause) {

        super(message, cause);
    }
}