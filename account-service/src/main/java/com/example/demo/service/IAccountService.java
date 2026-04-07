package com.example.demo.service;

import com.example.demo.dto.AccountResponse;
import com.example.demo.dto.CreateAccountRequest;

import java.math.BigDecimal;
import java.util.List;

public interface IAccountService {
    AccountResponse createAccount(String userId, CreateAccountRequest request);
    List<AccountResponse> listAccounts(String userId);
    AccountResponse getAccount(String userId, String accountNumber);
    void deleteAccount(String userId, String accountNumber);
    AccountResponse getAccountInternal(String accountNumber);
    AccountResponse credit(String accountNumber, BigDecimal amount);
    AccountResponse debit(String accountNumber, BigDecimal amount);
}
