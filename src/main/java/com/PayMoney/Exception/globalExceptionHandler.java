package com.PayMoney.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class globalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationException(
            MethodArgumentNotValidException exception) {

        Map<String, String> errors = new HashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage())
                );

        return errors;
    }

    @ExceptionHandler(insufficientBalanceException.class)
    public ResponseEntity<Map<String, String>> handleInsufficientBalance(
            insufficientBalanceException exception) {

        Map<String, String> response = new HashMap<>();
        response.put("message", exception.getMessage());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(walletNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleWalletNotFound(
            walletNotFoundException exception) {

        Map<String, String> response = new HashMap<>();
        response.put("message", exception.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(invalidTransactionException.class)
    public ResponseEntity<Map<String, String>> handleInvalidTransaction(
            invalidTransactionException exception) {

        Map<String, String> response = new HashMap<>();
        response.put("message", exception.getMessage());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(transactionNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleTransactionNotFound(
            transactionNotFoundException exception) {

        Map<String, String> response = new HashMap<>();
        response.put("message", exception.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}