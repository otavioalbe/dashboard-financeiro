package com.example.demo.service;

import com.example.demo.dto.AccountResponse;
import com.example.demo.dto.CreateAccountRequest;
import com.example.demo.entity.Account;
import com.example.demo.exception.AccountAccessDeniedException;
import com.example.demo.exception.AccountNotFoundException;
import com.example.demo.exception.InsufficientBalanceException;
import com.example.demo.mapper.AccountMapper;
import com.example.demo.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService implements IAccountService {

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
        Account account = findOrThrow(accountNumber);
        validateOwnership(account, userId);
        return accountMapper.toResponse(account);
    }

    @Transactional
    public void deleteAccount(String userId, String accountNumber) {
        Account account = findOrThrow(accountNumber);
        validateOwnership(account, userId);
        accountRepository.deleteById(accountNumber);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountInternal(String accountNumber) {
        return accountMapper.toResponse(findOrThrow(accountNumber));
    }

    @Transactional
    public AccountResponse credit(String userId, String accountNumber, BigDecimal amount) {
        Account account = accountRepository.findByIdWithLock(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
        validateOwnership(account, userId);
        account.setBalance(account.getBalance().add(amount));
        return accountMapper.toResponse(accountRepository.save(account));
    }

    @Transactional
    public AccountResponse debit(String userId, String accountNumber, BigDecimal amount) {
        Account account = accountRepository.findByIdWithLock(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
        validateOwnership(account, userId);
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException();
        }
        account.setBalance(account.getBalance().subtract(amount));
        return accountMapper.toResponse(accountRepository.save(account));
    }

    private Account findOrThrow(String accountNumber) {
        return accountRepository.findById(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
    }

    private void validateOwnership(Account account, String userId) {
        if (!account.getUserId().equals(userId)) {
            throw new AccountAccessDeniedException();
        }
    }

    private String generateAccountNumber() {
        String number;
        do {
            number = String.format("%010d", (long) (secureRandom.nextLong(10_000_000_000L)));
        } while (accountRepository.existsById(number));
        return number;
    }
}
