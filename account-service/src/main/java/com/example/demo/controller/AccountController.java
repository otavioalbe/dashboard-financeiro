package com.example.demo.controller;

import com.example.demo.dto.AccountResponse;
import com.example.demo.dto.CreateAccountRequest;
import com.example.demo.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Account management endpoints")
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<List<AccountResponse>> listAccounts(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(accountService.listAccounts(userId));
    }

    @PostMapping("/create-account")
    public ResponseEntity<AccountResponse> createAccount(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.createAccount(userId, request));
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getAccount(accountNumber));
    }

    @DeleteMapping("/{accountNumber}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String accountNumber) {
        accountService.deleteAccount(accountNumber);
        return ResponseEntity.noContent().build();
    }

}
