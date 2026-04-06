package com.example.demo.dto;

import com.example.demo.entity.TransactionCategory;
import com.example.demo.entity.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateTransactionRequest(

        @NotBlank(message = "accountNumber is required")
        String accountNumber,

        @NotNull(message = "type is required")
        TransactionType type,

        @NotNull(message = "category is required")
        TransactionCategory category,

        // Livre: "da mãe", "pizza sexta", "bolsa família", etc.
        String description,

        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be greater than zero")
        BigDecimal amount,

        // Obrigatório apenas quando type = TRANSFER
        String targetAccountNumber
) {}
