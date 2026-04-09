package com.example.demo.document;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "transaction_records")
@CompoundIndex(name = "userId_createdAt", def = "{'userId': 1, 'createdAt': -1}")
@Builder
@Getter
public class TransactionRecord {

    @Id
    private String id;

    private String transactionId;
    private String userId;
    private String accountNumber;
    private String targetAccountNumber;
    private String type;
    private String category;
    private String description;
    private BigDecimal amount;
    private LocalDateTime createdAt;
}
