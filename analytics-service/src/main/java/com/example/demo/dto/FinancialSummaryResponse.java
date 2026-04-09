package com.example.demo.dto;

import java.math.BigDecimal;

public record FinancialSummaryResponse(
        BigDecimal totalCredit,
        BigDecimal totalDebit,
        BigDecimal netBalance,
        long transactionCount
) {}
