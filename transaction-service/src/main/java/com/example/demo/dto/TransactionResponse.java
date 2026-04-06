package com.example.demo.dto;

import com.example.demo.entity.TransactionCategory;
import com.example.demo.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        String id,
        String accountNumber,
        String targetAccountNumber,
        TransactionType type,
        TransactionCategory category,
        String description,
        BigDecimal amount,
        LocalDateTime createdAt
) {}
