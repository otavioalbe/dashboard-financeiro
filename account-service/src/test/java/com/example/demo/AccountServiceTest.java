package com.example.demo;

import com.example.demo.dto.AccountResponse;
import com.example.demo.dto.CreateAccountRequest;
import com.example.demo.entity.Account;
import com.example.demo.entity.AccountStatus;
import com.example.demo.entity.AccountType;
import com.example.demo.exception.AccountAccessDeniedException;
import com.example.demo.exception.AccountNotFoundException;
import com.example.demo.exception.InsufficientBalanceException;
import com.example.demo.mapper.AccountMapper;
import com.example.demo.repository.AccountRepository;
import com.example.demo.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService unit tests")
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountService accountService;

    private Account account;
    private AccountResponse accountResponse;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .accountNumber("0123456789")
                .userId("otavio")
                .type(AccountType.CHECKING)
                .balance(new BigDecimal("1000.00"))
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        accountResponse = new AccountResponse(
                "0123456789", "otavio", AccountType.CHECKING,
                new BigDecimal("1000.00"), AccountStatus.ACTIVE, LocalDateTime.now()
        );
    }

    // --- createAccount ---

    @Test
    @DisplayName("createAccount: deve criar conta e retornar response")
    void createAccount_success() {
        when(accountRepository.existsById(any())).thenReturn(false);
        when(accountRepository.save(any())).thenReturn(account);
        when(accountMapper.toResponse(account)).thenReturn(accountResponse);

        AccountResponse result = accountService.createAccount("otavio", new CreateAccountRequest(AccountType.CHECKING));

        assertThat(result.userId()).isEqualTo("otavio");
        assertThat(result.type()).isEqualTo(AccountType.CHECKING);
        verify(accountRepository).save(any());
    }

    // --- listAccounts ---

    @Test
    @DisplayName("listAccounts: deve retornar todas as contas do usuário")
    void listAccounts_success() {
        when(accountRepository.findByUserId("otavio")).thenReturn(List.of(account));
        when(accountMapper.toResponse(account)).thenReturn(accountResponse);

        List<AccountResponse> result = accountService.listAccounts("otavio");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).accountNumber()).isEqualTo("0123456789");
    }

    // --- getAccount ---

    @Test
    @DisplayName("getAccount: deve retornar conta quando userId bate")
    void getAccount_success() {
        when(accountRepository.findById("0123456789")).thenReturn(Optional.of(account));
        when(accountMapper.toResponse(account)).thenReturn(accountResponse);

        AccountResponse result = accountService.getAccount("otavio", "0123456789");

        assertThat(result.accountNumber()).isEqualTo("0123456789");
    }

    @Test
    @DisplayName("getAccount: deve lançar AccountNotFoundException quando conta não existe")
    void getAccount_notFound() {
        when(accountRepository.findById("0000000000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccount("otavio", "0000000000"))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("0000000000");
    }

    @Test
    @DisplayName("getAccount: deve lançar AccountAccessDeniedException quando userId não bate")
    void getAccount_wrongUser() {
        when(accountRepository.findById("0123456789")).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.getAccount("outro-usuario", "0123456789"))
                .isInstanceOf(AccountAccessDeniedException.class);
    }

    // --- deleteAccount ---

    @Test
    @DisplayName("deleteAccount: deve deletar conta do usuário correto")
    void deleteAccount_success() {
        when(accountRepository.findById("0123456789")).thenReturn(Optional.of(account));

        accountService.deleteAccount("otavio", "0123456789");

        verify(accountRepository).deleteById("0123456789");
    }

    @Test
    @DisplayName("deleteAccount: deve lançar AccountNotFoundException")
    void deleteAccount_notFound() {
        when(accountRepository.findById("0000000000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.deleteAccount("otavio", "0000000000"))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    @DisplayName("deleteAccount: deve lançar AccountAccessDeniedException quando userId não bate")
    void deleteAccount_wrongUser() {
        when(accountRepository.findById("0123456789")).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.deleteAccount("outro-usuario", "0123456789"))
                .isInstanceOf(AccountAccessDeniedException.class);

        verify(accountRepository, never()).deleteById(any());
    }

    // --- credit ---

    @Test
    @DisplayName("credit: deve adicionar saldo corretamente")
    void credit_success() {
        when(accountRepository.findByIdWithLock("0123456789")).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);
        when(accountMapper.toResponse(account)).thenReturn(accountResponse);

        accountService.credit("otavio", "0123456789", new BigDecimal("500.00"));

        assertThat(account.getBalance()).isEqualByComparingTo("1500.00");
        verify(accountRepository).save(account);
    }

    @Test
    @DisplayName("credit: deve lançar AccountNotFoundException quando conta não existe")
    void credit_notFound() {
        when(accountRepository.findByIdWithLock("0000000000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.credit("otavio", "0000000000", new BigDecimal("100.00")))
                .isInstanceOf(AccountNotFoundException.class);
    }

    // --- debit ---

    @Test
    @DisplayName("debit: deve subtrair saldo corretamente")
    void debit_success() {
        when(accountRepository.findByIdWithLock("0123456789")).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);
        when(accountMapper.toResponse(account)).thenReturn(accountResponse);

        accountService.debit("otavio", "0123456789", new BigDecimal("300.00"));

        assertThat(account.getBalance()).isEqualByComparingTo("700.00");
        verify(accountRepository).save(account);
    }

    @Test
    @DisplayName("debit: deve lançar InsufficientBalanceException quando saldo insuficiente")
    void debit_insufficientBalance() {
        when(accountRepository.findByIdWithLock("0123456789")).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.debit("otavio", "0123456789", new BigDecimal("9999.00")))
                .isInstanceOf(InsufficientBalanceException.class);

        verify(accountRepository, never()).save(any());
    }
}
