package com.example.demo.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(AccountNotFoundException ex) {
        return ResponseEntity.status(404).body(
                new ErrorResponse("ACCOUNT_NOT_FOUND", ex.getMessage(), LocalDateTime.now())
        );
    }

    @ExceptionHandler(AccountAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handle(AccountAccessDeniedException ex) {
        return ResponseEntity.status(403).body(
                new ErrorResponse("ACCESS_DENIED", ex.getMessage(), LocalDateTime.now())
        );
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handle(InsufficientBalanceException ex) {
        return ResponseEntity.status(422).body(
                new ErrorResponse("INSUFFICIENT_BALANCE", ex.getMessage(), LocalDateTime.now())
        );
    }

    @ExceptionHandler(InvalidTransferException.class)
    public ResponseEntity<ErrorResponse> handle(InvalidTransferException ex) {
        return ResponseEntity.status(400).body(
                new ErrorResponse("INVALID_TRANSFER", ex.getMessage(), LocalDateTime.now())
        );
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(TransactionNotFoundException ex) {
        return ResponseEntity.status(404).body(
                new ErrorResponse("TRANSACTION_NOT_FOUND", ex.getMessage(), LocalDateTime.now())
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handle(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(400).body(
                new ErrorResponse("VALIDATION_ERROR", message, LocalDateTime.now())
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handle(Exception ex) {
        return ResponseEntity.status(500).body(
                new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred", LocalDateTime.now())
        );
    }
}
