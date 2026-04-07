package com.example.demo.service;

import com.example.demo.dto.CreateTransactionRequest;
import com.example.demo.dto.TransactionResponse;

import java.util.List;

public interface ITransactionOperationService {
    TransactionResponse create(String userId, CreateTransactionRequest req);
    List<TransactionResponse> listByUser(String userId);
    List<TransactionResponse> listByAccount(String userId, String accountNumber);
    TransactionResponse getById(String userId, String transactionId);
}
