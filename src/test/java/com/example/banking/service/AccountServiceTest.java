package com.example.banking.service;

import com.example.banking.dto.TransactionResponse;
import com.example.banking.entity.Account;
import com.example.banking.entity.Transaction;
import com.example.banking.enums.TransactionType;
import com.example.banking.exception.AccountNotFoundException;
import com.example.banking.exception.InsufficientFundsException;
import com.example.banking.exception.InvalidAmountException;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    @Test
    void shouldCreateAccountSuccessfully() {

        UUID accountId = UUID.randomUUID();

        Account savedAccount = Account.builder()
                .accountId(accountId)
                .balance(BigDecimal.ZERO)
                .build();

        when(accountRepository.save(any(Account.class)))
                .thenReturn(savedAccount);

        UUID result = accountService.createAccount();

        assertNotNull(result);
        assertEquals(accountId, result);

        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void shouldDepositSuccessfully() {

        UUID accountId = UUID.randomUUID();

        Account account = buildAccount(BigDecimal.valueOf(1000));

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        accountService.deposit(accountId, BigDecimal.valueOf(500));

        assertEquals(
                BigDecimal.valueOf(1500),
                account.getBalance()
        );

        verify(accountRepository).save(account);

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void shouldRecordDepositTransaction() {

        UUID accountId = UUID.randomUUID();

        Account account = buildAccount(BigDecimal.valueOf(1000));

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        accountService.deposit(accountId, BigDecimal.valueOf(250));

        ArgumentCaptor<Transaction> captor =
                ArgumentCaptor.forClass(Transaction.class);

        verify(transactionRepository).save(captor.capture());

        Transaction transaction = captor.getValue();

        assertEquals(
                TransactionType.DEPOSIT,
                transaction.getTransactionType()
        );

        assertEquals(
                BigDecimal.valueOf(250),
                transaction.getAmount()
        );

        assertEquals(
                account,
                transaction.getAccount()
        );

        assertNotNull(transaction.getTimestamp());
    }

    @Test
    void shouldThrowExceptionWhenDepositAmountIsZero() {

        UUID accountId = UUID.randomUUID();

        assertThrows(
                InvalidAmountException.class,
                () -> accountService.deposit(
                        accountId,
                        BigDecimal.ZERO
                )
        );

        verify(accountRepository, never()).findById(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenDepositAmountIsNegative() {

        UUID accountId = UUID.randomUUID();

        assertThrows(
                InvalidAmountException.class,
                () -> accountService.deposit(
                        accountId,
                        BigDecimal.valueOf(-100)
                )
        );

        verify(accountRepository, never()).findById(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenDepositAccountDoesNotExist() {

        UUID accountId = UUID.randomUUID();

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.empty());

        assertThrows(
                AccountNotFoundException.class,
                () -> accountService.deposit(
                        accountId,
                        BigDecimal.valueOf(500)
                )
        );

        verify(accountRepository).findById(accountId);
        verify(accountRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldWithdrawSuccessfully() {

        UUID accountId = UUID.randomUUID();

        Account account = buildAccount(BigDecimal.valueOf(1000));

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        accountService.withdraw(accountId, BigDecimal.valueOf(400));

        assertEquals(
                BigDecimal.valueOf(600),
                account.getBalance()
        );

        verify(accountRepository).save(account);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void shouldRecordWithdrawalTransaction() {

        UUID accountId = UUID.randomUUID();

        Account account = buildAccount(BigDecimal.valueOf(1000));

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        accountService.withdraw(accountId, BigDecimal.valueOf(200));

        ArgumentCaptor<Transaction> captor =
                ArgumentCaptor.forClass(Transaction.class);

        verify(transactionRepository).save(captor.capture());

        Transaction transaction = captor.getValue();

        assertEquals(TransactionType.WITHDRAWAL, transaction.getTransactionType());
        assertEquals(BigDecimal.valueOf(200), transaction.getAmount());
        assertEquals(account, transaction.getAccount());
    }

    @Test
    void shouldThrowExceptionWhenWithdrawalAmountIsZero() {

        UUID accountId = UUID.randomUUID();

        assertThrows(
                InvalidAmountException.class,
                () -> accountService.withdraw(accountId, BigDecimal.ZERO)
        );

        verify(accountRepository, never()).findById(any());
    }

    @Test
    void shouldThrowExceptionWhenWithdrawalAmountIsNegative() {

        UUID accountId = UUID.randomUUID();

        assertThrows(
                InvalidAmountException.class,
                () -> accountService.withdraw(accountId, BigDecimal.valueOf(-10))
        );

        verify(accountRepository, never()).findById(any());
    }

    @Test
    void shouldThrowExceptionWhenInsufficientFunds() {

        UUID accountId = UUID.randomUUID();

        Account account = buildAccount(BigDecimal.valueOf(500));

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        assertThrows(
                InsufficientFundsException.class,
                () -> accountService.withdraw(accountId, BigDecimal.valueOf(600))
        );

        verify(accountRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenWithdrawAccountDoesNotExist() {

        UUID accountId = UUID.randomUUID();

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.empty());

        assertThrows(
                AccountNotFoundException.class,
                () -> accountService.withdraw(accountId, BigDecimal.valueOf(100))
        );

        verify(accountRepository).findById(accountId);
    }

    @Test
    void shouldTransferSuccessfully() {

        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();

        Account from = Account.builder()
                .accountId(fromId)
                .balance(BigDecimal.valueOf(1000))
                .build();

        Account to = Account.builder()
                .accountId(toId)
                .balance(BigDecimal.valueOf(500))
                .build();

        when(accountRepository.findById(fromId))
                .thenReturn(Optional.of(from));

        when(accountRepository.findById(toId))
                .thenReturn(Optional.of(to));

        accountService.transfer(
                fromId,
                toId,
                BigDecimal.valueOf(300)
        );

        assertEquals(BigDecimal.valueOf(700), from.getBalance());
        assertEquals(BigDecimal.valueOf(800), to.getBalance());

        verify(accountRepository, times(2)).save(any(Account.class));
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }

    @Test
    void shouldThrowExceptionWhenTransferAmountIsZero() {

        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();

        assertThrows(
                InvalidAmountException.class,
                () -> accountService.transfer(
                        fromId,
                        toId,
                        BigDecimal.ZERO
                )
        );

        verify(accountRepository, never()).findById(any());
    }

    @Test
    void shouldThrowExceptionWhenTransferToSameAccount() {

        UUID accountId = UUID.randomUUID();

        assertThrows(
                InvalidAmountException.class,
                () -> accountService.transfer(
                        accountId,
                        accountId,
                        BigDecimal.valueOf(100)
                )
        );

        verify(accountRepository, never()).findById(any());
    }

    @Test
    void shouldThrowExceptionWhenTransferHasInsufficientFunds() {

        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();

        Account from = Account.builder()
                .accountId(fromId)
                .balance(BigDecimal.valueOf(200))
                .build();

        Account to = Account.builder()
                .accountId(toId)
                .balance(BigDecimal.valueOf(100))
                .build();

        when(accountRepository.findById(fromId))
                .thenReturn(Optional.of(from));

        when(accountRepository.findById(toId))
                .thenReturn(Optional.of(to));

        assertThrows(
                InsufficientFundsException.class,
                () -> accountService.transfer(
                        fromId,
                        toId,
                        BigDecimal.valueOf(500)
                )
        );

        verify(accountRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenSourceAccountDoesNotExist() {

        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();

        when(accountRepository.findById(fromId))
                .thenReturn(Optional.empty());

        assertThrows(
                AccountNotFoundException.class,
                () -> accountService.transfer(
                        fromId,
                        toId,
                        BigDecimal.valueOf(100)
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenDestinationAccountDoesNotExist() {

        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();

        Account from = Account.builder()
                .accountId(fromId)
                .balance(BigDecimal.valueOf(1000))
                .build();

        when(accountRepository.findById(fromId))
                .thenReturn(Optional.of(from));

        when(accountRepository.findById(toId))
                .thenReturn(Optional.empty());

        assertThrows(
                AccountNotFoundException.class,
                () -> accountService.transfer(
                        fromId,
                        toId,
                        BigDecimal.valueOf(100)
                )
        );
    }

    @Test
    void shouldReturnBalanceSuccessfully() {

        UUID accountId = UUID.randomUUID();

        Account account = buildAccount(BigDecimal.valueOf(2500));

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        BigDecimal balance = accountService.getBalance(accountId);

        assertEquals(BigDecimal.valueOf(2500), balance);

        verify(accountRepository).findById(accountId);
    }

    @Test
    void shouldThrowExceptionWhenGettingBalanceForUnknownAccount() {

        UUID accountId = UUID.randomUUID();

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.empty());

        assertThrows(
                AccountNotFoundException.class,
                () -> accountService.getBalance(accountId)
        );

        verify(accountRepository).findById(accountId);
    }

    @Test
    void shouldReturnTransactionsSuccessfully() {

        UUID accountId = UUID.randomUUID();

Account account = buildAccount(BigDecimal.valueOf(1000));

        Transaction transaction1 = Transaction.builder()
                .transactionId(UUID.randomUUID())
                .account(account)
                .transactionType(TransactionType.DEPOSIT)
                .amount(BigDecimal.valueOf(500))
                .timestamp(LocalDateTime.now())
                .build();

        Transaction transaction2 = Transaction.builder()
                .transactionId(UUID.randomUUID())
                .account(account)
                .transactionType(TransactionType.WITHDRAWAL)
                .amount(BigDecimal.valueOf(200))
                .timestamp(LocalDateTime.now().minusMinutes(5))
                .build();

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        when(transactionRepository.findByAccountOrderByTimestampDesc(account))
                .thenReturn(List.of(transaction1, transaction2));

        List<TransactionResponse> response =
                accountService.getTransactions(accountId);

        assertEquals(2, response.size());

        assertEquals(
                TransactionType.DEPOSIT,
                response.get(0).transactionType()
        );

        assertEquals(
                BigDecimal.valueOf(500),
                response.get(0).amount()
        );

        verify(transactionRepository)
                .findByAccountOrderByTimestampDesc(account);
    }

    @Test
    void shouldReturnEmptyTransactionList() {

        UUID accountId = UUID.randomUUID();

        Account account = buildAccount(BigDecimal.ZERO);

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        when(transactionRepository.findByAccountOrderByTimestampDesc(account))
                .thenReturn(List.of());

        List<TransactionResponse> response =
                accountService.getTransactions(accountId);

        assertTrue(response.isEmpty());

        verify(transactionRepository)
                .findByAccountOrderByTimestampDesc(account);
    }

    @Test
    void shouldThrowExceptionWhenGettingTransactionsForUnknownAccount() {

        UUID accountId = UUID.randomUUID();

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.empty());

        assertThrows(
                AccountNotFoundException.class,
                () -> accountService.getTransactions(accountId)
        );

        verify(accountRepository).findById(accountId);

        verify(transactionRepository, never())
                .findByAccountOrderByTimestampDesc(any());
    }

    private Account buildAccount(BigDecimal balance) {
        Account account = new Account();
        account.setAccountId(UUID.randomUUID());
        account.setBalance(balance);
        return account;
    }
}