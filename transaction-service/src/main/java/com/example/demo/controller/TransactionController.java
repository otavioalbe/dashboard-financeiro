package com.example.demo.controller;

import com.example.demo.dto.CreateTransactionRequest;
import com.example.demo.dto.TransactionResponse;
import com.example.demo.service.ITransactionOperationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Transaction management endpoints")
public class TransactionController {

    private final ITransactionOperationService transactionService;

    @PostMapping
    @Operation(summary = "Create a transaction (CREDIT, DEBIT or TRANSFER)")
    public ResponseEntity<TransactionResponse> create(
            Authentication authentication,
            @Valid @RequestBody CreateTransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.create(authentication.getName(), request));
    }

    @GetMapping
    @Operation(summary = "List all transactions of the authenticated user")
    public ResponseEntity<List<TransactionResponse>> listByUser(Authentication authentication) {
        return ResponseEntity.ok(transactionService.listByUser(authentication.getName()));
    }

    @GetMapping("/account/{accountNumber}")
    @Operation(summary = "List transactions by account number")
    public ResponseEntity<List<TransactionResponse>> listByAccount(
            Authentication authentication,
            @PathVariable String accountNumber) {
        return ResponseEntity.ok(transactionService.listByAccount(authentication.getName(), accountNumber));
    }

    @GetMapping("/{transactionId}")
    @Operation(summary = "Get a specific transaction by ID")
    public ResponseEntity<TransactionResponse> getById(
            Authentication authentication,
            @PathVariable String transactionId) {
        return ResponseEntity.ok(transactionService.getById(authentication.getName(), transactionId));
    }
}
