package com.example.demo.dto;

import com.example.demo.entity.AccountType;
import jakarta.validation.constraints.NotNull;

public record CreateAccountRequest(
        @NotNull(message = "type is required") AccountType type
) {}
