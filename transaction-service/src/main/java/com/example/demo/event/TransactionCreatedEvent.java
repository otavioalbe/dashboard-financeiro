package com.example.demo.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionCreatedEvent(
        String transactionId,
        String userId,
        String accountNumber,
        String targetAccountNumber,
        String type,
        String category,
        String description,
        BigDecimal amount,
        LocalDateTime createdAt
) {}
