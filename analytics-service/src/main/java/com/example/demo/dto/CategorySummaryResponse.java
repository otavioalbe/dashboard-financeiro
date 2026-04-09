package com.example.demo.dto;

import java.math.BigDecimal;

public record CategorySummaryResponse(
        String category,
        String type,
        BigDecimal total,
        long count
) {}
