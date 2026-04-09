package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TopExpenseResponse(
        String transactionId,
        String category,
        String description,
        BigDecimal amount,
        LocalDateTime createdAt
) {}
