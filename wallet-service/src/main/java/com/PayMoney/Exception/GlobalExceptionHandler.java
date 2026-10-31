
package com.PayMoney.Exception;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException exception) {

        Map<String, String> errors = new HashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.putIfAbsent(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Validation failed");
        response.put("errors", errors);

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(
            IllegalArgumentException ex) {

        Map<String, String> error = new HashMap<>();
        error.put("error", "Invalid Request");
        error.put("message", ex.getMessage());

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleMalformedJson(
            HttpMessageNotReadableException exception) {

        return ResponseEntity.badRequest().body(
                Map.of(
                        "message",
                        "Invalid request body. Check the JSON format and field types."
                )
        );
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Map<String, String>> handleMissingHeader(
            MissingRequestHeaderException exception) {

        return ResponseEntity.badRequest().body(
                Map.of(
                        "message",
                        exception.getHeaderName() + " header is required"
                )
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, String>> handleMissingParameter(
            MissingServletRequestParameterException exception) {

        return ResponseEntity.badRequest().body(
                Map.of(
                        "message",
                        exception.getParameterName() + " parameter is required"
                )
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception) {

        return ResponseEntity.badRequest().body(
                Map.of(
                        "message",
                        "Invalid value for parameter: " + exception.getName()
                )
        );
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<Map<String, String>> handleInsufficientBalance(
            InsufficientBalanceException exception) {

        return ResponseEntity.badRequest()
                .body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleWalletNotFound(
            WalletNotFoundException exception) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(InvalidTransactionException.class)
    public ResponseEntity<Map<String, String>> handleInvalidTransaction(
            InvalidTransactionException exception) {

        return ResponseEntity.badRequest()
                .body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleTransactionNotFound(
            TransactionNotFoundException exception) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", exception.getMessage()));
    }

    // Without these, the catch-all handler below turns them into a 500.
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(
            AccessDeniedException exception) {

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "You do not have permission to do this"));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatus(
            ResponseStatusException exception) {

        String reason = exception.getReason() != null
                ? exception.getReason() : "Request failed";

        return ResponseEntity.status(exception.getStatusCode())
                .body(Map.of("message", reason));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, String>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception) {

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(Map.of("message", "HTTP method not supported for this endpoint"));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFound(
            NoResourceFoundException exception) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error", "Not Found",
                        "message", "The requested resource was not found"
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpectedException(
            Exception exception) {

        log.error("Unexpected application error", exception);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "An unexpected error occurred"));
    }
}