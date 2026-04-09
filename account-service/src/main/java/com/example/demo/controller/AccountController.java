package com.example.demo.controller;

import com.example.demo.dto.AccountResponse;
import com.example.demo.dto.BalanceRequest;
import com.example.demo.dto.CreateAccountRequest;
import com.example.demo.service.IAccountService;
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
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Account management endpoints")
public class AccountController {

    private final IAccountService accountService;

    @GetMapping
    public ResponseEntity<List<AccountResponse>> listAccounts(Authentication authentication) {
        return ResponseEntity.ok(accountService.listAccounts(authentication.getName()));
    }

    @PostMapping("/create-account")
    public ResponseEntity<AccountResponse> createAccount(
            Authentication authentication,
            @Valid @RequestBody CreateAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.createAccount(authentication.getName(), request));
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccount(
            Authentication authentication,
            @PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getAccount(authentication.getName(), accountNumber));
    }

    @DeleteMapping("/{accountNumber}")
    public ResponseEntity<Void> deleteAccount(
            Authentication authentication,
            @PathVariable String accountNumber) {
        accountService.deleteAccount(authentication.getName(), accountNumber);
        return ResponseEntity.noContent().build();
    }

    // --- Internal endpoints (transaction-service only, protected by X-Internal-Key) ---

    @GetMapping("/internal/{accountNumber}")
    @Operation(summary = "Internal: get account info for service-to-service calls")
    public ResponseEntity<AccountResponse> getAccountInternal(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getAccountInternal(accountNumber));
    }

    @PostMapping("/internal/{accountNumber}/credit")
    @Operation(summary = "Internal: credit amount to account balance")
    public ResponseEntity<AccountResponse> creditInternal(
            @PathVariable String accountNumber,
            @Valid @RequestBody BalanceRequest request) {
        return ResponseEntity.ok(accountService.credit(request.userId(), accountNumber, request.amount()));
    }

    @PostMapping("/internal/{accountNumber}/debit")
    @Operation(summary = "Internal: debit amount from account balance")
    public ResponseEntity<AccountResponse> debitInternal(
            @PathVariable String accountNumber,
            @Valid @RequestBody BalanceRequest request) {
        return ResponseEntity.ok(accountService.debit(request.userId(), accountNumber, request.amount()));
    }

}
