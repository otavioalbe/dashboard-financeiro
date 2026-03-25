package com.example.demo.service;

import com.example.demo.dto.AccountResponse;
import com.example.demo.dto.CreateAccountRequest;
import com.example.demo.entity.Account;
import com.example.demo.mapper.AccountMapper;
import com.example.demo.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

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
    public AccountResponse getAccount(String accountNumber) {
        return accountRepository.findById(accountNumber)
                .map(accountMapper::toResponse)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));
    }

    @Transactional
    public void deleteAccount(String accountNumber) {
        if (!accountRepository.existsById(accountNumber)) {
            throw new RuntimeException("Account not found: " + accountNumber);
        }
        accountRepository.deleteById(accountNumber);
    }

    private String generateAccountNumber() {
        String number;
        do {
            number = String.format("%010d", (long) (Math.random() * 10_000_000_000L));
        } while (accountRepository.existsById(number));
        return number;
    }
}
