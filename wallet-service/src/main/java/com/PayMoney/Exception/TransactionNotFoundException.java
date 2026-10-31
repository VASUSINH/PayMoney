package com.PayMoney.Exception;

public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(String message) {

        super(message);
    }
}
