package com.example.demo.consumer;

import com.example.demo.document.TransactionRecord;
import com.example.demo.event.TransactionCreatedEvent;
import com.example.demo.repository.TransactionRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventConsumer {

    private final TransactionRecordRepository repository;

    @KafkaListener(topics = "transactions", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(TransactionCreatedEvent event) {
        log.info("Received transaction event: transactionId={}, userId={}, type={}, amount={}",
                event.transactionId(), event.userId(), event.type(), event.amount());

        repository.save(TransactionRecord.builder()
                .transactionId(event.transactionId())
                .userId(event.userId())
                .accountNumber(event.accountNumber())
                .targetAccountNumber(event.targetAccountNumber())
                .type(event.type())
                .category(event.category())
                .description(event.description())
                .amount(event.amount())
                .createdAt(event.createdAt())
                .build());
    }
}
