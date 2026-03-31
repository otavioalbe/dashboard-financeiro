package com.example.demo.service;

import com.example.demo.dto.AccountResponse;
import com.example.demo.dto.CreateAccountRequest;
import com.example.demo.entity.Account;
import com.example.demo.mapper.AccountMapper;
import com.example.demo.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public AccountResponse createAccount(String userId, CreateAccountRequest request) {
        Account account = Account.builder()
                .accountNumber(generateAccountNumber())
                .userId(userId)
                .type(request.type())
                .build();

        return accountMapper.toResponse(accountRepository.save(account));
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> listAccounts(String userId) {
        return accountRepository.findByUserId(userId).stream()
                .map(accountMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(String userId, String accountNumber) {
        Account account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        if (!account.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return accountMapper.toResponse(account);
    }

    @Transactional
    public void deleteAccount(String userId, String accountNumber) {
        Account account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        if (!account.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        accountRepository.deleteById(accountNumber);
    }

    private String generateAccountNumber() {
        String number;
        do {
            number = String.format("%010d", (long) (secureRandom.nextLong(10_000_000_000L)));
        } while (accountRepository.existsById(number));
        return number;
    }
}
