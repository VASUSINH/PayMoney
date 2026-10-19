package com.PayMoney.Exception;

public class InvalidTransactionException extends RuntimeException {

    public InvalidTransactionException(String message) {

        super(message);
    }
}
