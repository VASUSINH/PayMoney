package com.PayMoney.Exception;

public class invalidTransactionException extends RuntimeException {

    public invalidTransactionException(String message) {

        super(message);
    }
}
