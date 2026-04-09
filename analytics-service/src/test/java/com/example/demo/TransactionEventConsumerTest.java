package com.example.demo;

import com.example.demo.consumer.TransactionEventConsumer;
import com.example.demo.document.TransactionRecord;
import com.example.demo.event.TransactionCreatedEvent;
import com.example.demo.repository.TransactionRecordRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionEventConsumer unit tests")
class TransactionEventConsumerTest {

    @Mock
    private TransactionRecordRepository repository;

    @InjectMocks
    private TransactionEventConsumer consumer;

    @Test
    @DisplayName("consume: deve persistir o evento como TransactionRecord no MongoDB")
    void consume_savesTransactionRecord() {
        LocalDateTime now = LocalDateTime.now();
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                "tx-001",
                "otavio",
                "0123456789",
                null,
                "CREDIT",
                "SALARY",
                "Salário mensal",
                new BigDecimal("5000.00"),
                now
        );

        consumer.consume(event);

        ArgumentCaptor<TransactionRecord> captor = ArgumentCaptor.forClass(TransactionRecord.class);
        verify(repository).save(captor.capture());

        TransactionRecord saved = captor.getValue();
        assertThat(saved.getTransactionId()).isEqualTo("tx-001");
        assertThat(saved.getUserId()).isEqualTo("otavio");
        assertThat(saved.getAccountNumber()).isEqualTo("0123456789");
        assertThat(saved.getTargetAccountNumber()).isNull();
        assertThat(saved.getType()).isEqualTo("CREDIT");
        assertThat(saved.getCategory()).isEqualTo("SALARY");
        assertThat(saved.getDescription()).isEqualTo("Salário mensal");
        assertThat(saved.getAmount()).isEqualByComparingTo("5000.00");
        assertThat(saved.getCreatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("consume: deve persistir evento de transferência com targetAccountNumber")
    void consume_savesTransferEvent() {
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                "tx-002",
                "otavio",
                "0123456789",
                "9876543210",
                "TRANSFER",
                "TRANSFER",
                "Reserva de emergência",
                new BigDecimal("1000.00"),
                LocalDateTime.now()
        );

        consumer.consume(event);

        ArgumentCaptor<TransactionRecord> captor = ArgumentCaptor.forClass(TransactionRecord.class);
        verify(repository).save(captor.capture());

        TransactionRecord saved = captor.getValue();
        assertThat(saved.getTargetAccountNumber()).isEqualTo("9876543210");
        assertThat(saved.getType()).isEqualTo("TRANSFER");
    }
}
