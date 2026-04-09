package com.example.demo.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class AccountClient {

    private final RestClient restClient;
    private final String internalApiKey;

    public AccountClient(
            @Value("${services.account.url}") String accountServiceUrl,
            @Value("${internal.api-key}") String internalApiKey) {
        this.restClient = RestClient.builder().baseUrl(accountServiceUrl).build();
        this.internalApiKey = internalApiKey;
    }

    public AccountInfo getAccount(String accountNumber) {
        try {
            return restClient.get()
                    .uri("/api/accounts/internal/{number}", accountNumber)
                    .header("X-Internal-Key", internalApiKey)
                    .retrieve()
                    .body(AccountInfo.class);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found: " + accountNumber);
        }
    }

    public void credit(String userId, String accountNumber, BigDecimal amount) {
        restClient.post()
                .uri("/api/accounts/internal/{number}/credit", accountNumber)
                .header("X-Internal-Key", internalApiKey)
                .body(Map.of("amount", amount, "userId", userId))
                .retrieve()
                .toBodilessEntity();
    }

    public void debit(String userId, String accountNumber, BigDecimal amount) {
        restClient.post()
                .uri("/api/accounts/internal/{number}/debit", accountNumber)
                .header("X-Internal-Key", internalApiKey)
                .body(Map.of("amount", amount, "userId", userId))
                .retrieve()
                .toBodilessEntity();
    }
}
