package com.example.demo.exception;

public class AccountAccessDeniedException extends RuntimeException {
    public AccountAccessDeniedException() {
        super("Access denied to this account");
    }
}
