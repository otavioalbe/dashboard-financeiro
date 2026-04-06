package com.example.demo.client;

import java.math.BigDecimal;

public record AccountInfo(
        String accountNumber,
        String userId,
        String type,
        BigDecimal balance,
        String status
) {}
