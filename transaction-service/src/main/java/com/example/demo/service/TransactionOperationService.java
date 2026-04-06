package com.example.demo.service;

import com.example.demo.client.AccountClient;
import com.example.demo.client.AccountInfo;
import com.example.demo.dto.CreateTransactionRequest;
import com.example.demo.dto.TransactionResponse;
import com.example.demo.entity.Transaction;
import com.example.demo.entity.TransactionType;
import com.example.demo.event.TransactionCreatedEvent;
import com.example.demo.mapper.TransactionMapper;
import com.example.demo.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionOperationService {

    private static final String TRANSACTIONS_TOPIC = "transactions";

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final AccountClient accountClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public TransactionResponse create(String userId, CreateTransactionRequest req) {
        AccountInfo source = accountClient.getAccount(req.accountNumber());
        validateOwnership(source, userId);

        if (req.type() == TransactionType.TRANSFER) {
            validateTransfer(req, userId);
        }

        if (req.type() == TransactionType.DEBIT || req.type() == TransactionType.TRANSFER) {
            if (source.balance().compareTo(req.amount()) < 0) {
                throw new ResponseStatusException(HttpStatusCode.valueOf(422), "Insufficient balance");
            }
        }

        applyBalanceChanges(req);

        Transaction transaction = transactionRepository.save(Transaction.builder()
                .userId(userId)
                .accountNumber(req.accountNumber())
                .targetAccountNumber(req.targetAccountNumber())
                .type(req.type())
                .category(req.category())
                .description(req.description())
                .amount(req.amount())
                .build());

        kafkaTemplate.send(TRANSACTIONS_TOPIC, transaction.getId(), new TransactionCreatedEvent(
                transaction.getId(),
                transaction.getUserId(),
                transaction.getAccountNumber(),
                transaction.getTargetAccountNumber(),
                transaction.getType().name(),
                transaction.getCategory().name(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getCreatedAt()
        ));

        return transactionMapper.toResponse(transaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> listByUser(String userId) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> listByAccount(String userId, String accountNumber) {
        AccountInfo account = accountClient.getAccount(accountNumber);
        validateOwnership(account, userId);
        return transactionRepository.findByAccountNumberOrderByCreatedAtDesc(accountNumber)
                .stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TransactionResponse getById(String userId, String transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Transaction not found"));
        if (!transaction.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(403), "Access denied");
        }
        return transactionMapper.toResponse(transaction);
    }

    private void validateOwnership(AccountInfo account, String userId) {
        if (!account.userId().equals(userId)) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(403), "Account does not belong to this user");
        }
    }

    private void validateTransfer(CreateTransactionRequest req, String userId) {
        if (req.targetAccountNumber() == null || req.targetAccountNumber().isBlank()) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "targetAccountNumber is required for TRANSFER");
        }
        if (req.accountNumber().equals(req.targetAccountNumber())) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Source and target accounts must be different");
        }
        AccountInfo target = accountClient.getAccount(req.targetAccountNumber());
        validateOwnership(target, userId);
    }

    private void applyBalanceChanges(CreateTransactionRequest req) {
        switch (req.type()) {
            case CREDIT -> accountClient.credit(req.accountNumber(), req.amount());
            case DEBIT -> accountClient.debit(req.accountNumber(), req.amount());
            case TRANSFER -> {
                accountClient.debit(req.accountNumber(), req.amount());
                accountClient.credit(req.targetAccountNumber(), req.amount());
            }
        }
    }
}
