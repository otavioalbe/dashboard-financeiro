package com.example.demo.dto;

import com.example.demo.entity.AccountStatus;
import com.example.demo.entity.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountResponse(
        String accountNumber,
        String userId,
        AccountType type,
        BigDecimal balance,
        AccountStatus status,
        LocalDateTime createdAt
) {}
