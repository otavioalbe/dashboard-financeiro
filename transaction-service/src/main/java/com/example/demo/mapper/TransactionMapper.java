package com.example.demo.mapper;

import com.example.demo.dto.TransactionResponse;
import com.example.demo.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAccountNumber(),
                transaction.getTargetAccountNumber(),
                transaction.getType(),
                transaction.getCategory(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getCreatedAt()
        );
    }
}
