package com.PayMoney.Exception;

public class externalServiceException extends RuntimeException {

    public externalServiceException(
            String message,
            Throwable cause) {

        super(message, cause);
    }
}