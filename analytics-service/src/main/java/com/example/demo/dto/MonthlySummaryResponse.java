package com.example.demo.dto;

import java.math.BigDecimal;

public record MonthlySummaryResponse(
        int year,
        int month,
        BigDecimal totalCredit,
        BigDecimal totalDebit
) {}
