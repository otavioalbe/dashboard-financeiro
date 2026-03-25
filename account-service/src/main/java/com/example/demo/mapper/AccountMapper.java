package com.example.demo.mapper;

import com.example.demo.dto.AccountResponse;
import com.example.demo.entity.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getAccountNumber(),
                account.getUserId(),
                account.getType(),
                account.getBalance(),
                account.getStatus(),
                account.getCreatedAt()
        );
    }
}
