package com.example.demo;

import com.example.demo.client.AccountClient;
import com.example.demo.client.AccountInfo;
import com.example.demo.dto.CreateTransactionRequest;
import com.example.demo.dto.TransactionResponse;
import com.example.demo.entity.Transaction;
import com.example.demo.entity.TransactionCategory;
import com.example.demo.entity.TransactionType;
import com.example.demo.exception.AccountAccessDeniedException;
import com.example.demo.exception.InsufficientBalanceException;
import com.example.demo.exception.InvalidTransferException;
import com.example.demo.exception.TransactionNotFoundException;
import com.example.demo.mapper.TransactionMapper;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.service.TransactionOperationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionOperationService unit tests")
class TransactionOperationServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private AccountClient accountClient;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private TransactionOperationService transactionOperationService;

    private AccountInfo accountInfo;
    private Transaction transaction;
    private TransactionResponse transactionResponse;

    @BeforeEach
    void setUp() {
        accountInfo = new AccountInfo("0123456789", "otavio", "CHECKING", new BigDecimal("1000.00"), "ACTIVE");

        transaction = Transaction.builder()
                .id("tx-001")
                .userId("otavio")
                .accountNumber("0123456789")
                .type(TransactionType.CREDIT)
                .category(TransactionCategory.SALARY)
                .description("Salário")
                .amount(new BigDecimal("5000.00"))
                .createdAt(LocalDateTime.now())
                .build();

        transactionResponse = new TransactionResponse(
                "tx-001", "0123456789", null,
                TransactionType.CREDIT, TransactionCategory.SALARY,
                "Salário", new BigDecimal("5000.00"), LocalDateTime.now()
        );
    }

    // --- create CREDIT ---

    @Test
    @DisplayName("create CREDIT: deve registrar crédito e publicar evento Kafka")
    void create_credit_success() {
        var req = new CreateTransactionRequest(
                "0123456789", TransactionType.CREDIT, TransactionCategory.SALARY,
                "Salário", new BigDecimal("5000.00"), null
        );

        when(accountClient.getAccount("0123456789")).thenReturn(accountInfo);
        when(transactionRepository.save(any())).thenReturn(transaction);
        when(transactionMapper.toResponse(transaction)).thenReturn(transactionResponse);

        TransactionResponse result = transactionOperationService.create("otavio", req);

        assertThat(result.type()).isEqualTo(TransactionType.CREDIT);
        verify(accountClient).credit(eq("0123456789"), eq(new BigDecimal("5000.00")));
        verify(kafkaTemplate).send(eq("transactions"), any(), any());
    }

    // --- create DEBIT ---

    @Test
    @DisplayName("create DEBIT: deve registrar débito quando há saldo suficiente")
    void create_debit_success() {
        var req = new CreateTransactionRequest(
                "0123456789", TransactionType.DEBIT, TransactionCategory.FOOD,
                "Almoço", new BigDecimal("50.00"), null
        );

        when(accountClient.getAccount("0123456789")).thenReturn(accountInfo);
        when(transactionRepository.save(any())).thenReturn(transaction);
        when(transactionMapper.toResponse(any())).thenReturn(transactionResponse);

        transactionOperationService.create("otavio", req);

        verify(accountClient).debit(eq("0123456789"), eq(new BigDecimal("50.00")));
    }

    @Test
    @DisplayName("create DEBIT: deve lançar InsufficientBalanceException quando saldo insuficiente")
    void create_debit_insufficientBalance() {
        var req = new CreateTransactionRequest(
                "0123456789", TransactionType.DEBIT, TransactionCategory.FOOD,
                "Almoço", new BigDecimal("9999.00"), null
        );

        when(accountClient.getAccount("0123456789")).thenReturn(accountInfo);

        assertThatThrownBy(() -> transactionOperationService.create("otavio", req))
                .isInstanceOf(InsufficientBalanceException.class);

        verify(accountClient, never()).debit(any(), any());
        verify(transactionRepository, never()).save(any());
    }

    // --- create TRANSFER ---

    @Test
    @DisplayName("create TRANSFER: deve debitar origem e creditar destino")
    void create_transfer_success() {
        var target = new AccountInfo("9876543210", "otavio", "SAVINGS", new BigDecimal("0.00"), "ACTIVE");
        var req = new CreateTransactionRequest(
                "0123456789", TransactionType.TRANSFER, TransactionCategory.TRANSFER,
                "Reserva", new BigDecimal("200.00"), "9876543210"
        );

        when(accountClient.getAccount("0123456789")).thenReturn(accountInfo);
        when(accountClient.getAccount("9876543210")).thenReturn(target);
        when(transactionRepository.save(any())).thenReturn(transaction);
        when(transactionMapper.toResponse(any())).thenReturn(transactionResponse);

        transactionOperationService.create("otavio", req);

        verify(accountClient).debit(eq("0123456789"), eq(new BigDecimal("200.00")));
        verify(accountClient).credit(eq("9876543210"), eq(new BigDecimal("200.00")));
    }

    @Test
    @DisplayName("create TRANSFER: deve lançar InvalidTransferException quando targetAccountNumber está ausente")
    void create_transfer_missingTarget() {
        var req = new CreateTransactionRequest(
                "0123456789", TransactionType.TRANSFER, TransactionCategory.TRANSFER,
                "Reserva", new BigDecimal("200.00"), null
        );

        when(accountClient.getAccount("0123456789")).thenReturn(accountInfo);

        assertThatThrownBy(() -> transactionOperationService.create("otavio", req))
                .isInstanceOf(InvalidTransferException.class)
                .hasMessageContaining("targetAccountNumber");
    }

    @Test
    @DisplayName("create TRANSFER: deve lançar InvalidTransferException quando origem e destino são iguais")
    void create_transfer_sameAccount() {
        var req = new CreateTransactionRequest(
                "0123456789", TransactionType.TRANSFER, TransactionCategory.TRANSFER,
                "Reserva", new BigDecimal("200.00"), "0123456789"
        );

        when(accountClient.getAccount("0123456789")).thenReturn(accountInfo);

        assertThatThrownBy(() -> transactionOperationService.create("otavio", req))
                .isInstanceOf(InvalidTransferException.class)
                .hasMessageContaining("different");
    }

    @Test
    @DisplayName("create: deve lançar AccountAccessDeniedException quando conta não pertence ao usuário")
    void create_accountNotOwnedByUser() {
        var otherAccount = new AccountInfo("0123456789", "outro-usuario", "CHECKING", new BigDecimal("1000.00"), "ACTIVE");
        var req = new CreateTransactionRequest(
                "0123456789", TransactionType.CREDIT, TransactionCategory.SALARY,
                "Salário", new BigDecimal("100.00"), null
        );

        when(accountClient.getAccount("0123456789")).thenReturn(otherAccount);

        assertThatThrownBy(() -> transactionOperationService.create("otavio", req))
                .isInstanceOf(AccountAccessDeniedException.class);
    }

    // --- listByUser ---

    @Test
    @DisplayName("listByUser: deve retornar transações do usuário ordenadas por data")
    void listByUser_success() {
        when(transactionRepository.findByUserIdOrderByCreatedAtDesc("otavio")).thenReturn(List.of(transaction));
        when(transactionMapper.toResponse(transaction)).thenReturn(transactionResponse);

        List<TransactionResponse> result = transactionOperationService.listByUser("otavio");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo("tx-001");
    }

    // --- getById ---

    @Test
    @DisplayName("getById: deve retornar transação quando userId bate")
    void getById_success() {
        when(transactionRepository.findById("tx-001")).thenReturn(Optional.of(transaction));
        when(transactionMapper.toResponse(transaction)).thenReturn(transactionResponse);

        TransactionResponse result = transactionOperationService.getById("otavio", "tx-001");

        assertThat(result.id()).isEqualTo("tx-001");
    }

    @Test
    @DisplayName("getById: deve lançar TransactionNotFoundException quando não existe")
    void getById_notFound() {
        when(transactionRepository.findById("tx-999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionOperationService.getById("otavio", "tx-999"))
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessageContaining("tx-999");
    }

    @Test
    @DisplayName("getById: deve lançar AccountAccessDeniedException quando userId não bate")
    void getById_wrongUser() {
        when(transactionRepository.findById("tx-001")).thenReturn(Optional.of(transaction));

        assertThatThrownBy(() -> transactionOperationService.getById("outro-usuario", "tx-001"))
                .isInstanceOf(AccountAccessDeniedException.class);
    }
}
