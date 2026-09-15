package com.PayMoney.Exception;

public class transactionNotFoundException extends RuntimeException {
    public transactionNotFoundException(String message) {

        super(message);
    }
}
