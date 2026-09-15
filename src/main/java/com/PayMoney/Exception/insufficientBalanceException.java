package com.PayMoney.Exception;

public class insufficientBalanceException extends RuntimeException{

        public insufficientBalanceException(String message) {
            super(message);
        }
    }

