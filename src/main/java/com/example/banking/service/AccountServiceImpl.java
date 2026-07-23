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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public UUID createAccount() {

        Account account = Account.builder()
                .balance(BigDecimal.ZERO)
                .build();

        Account savedAccount = accountRepository.save(account);

        return savedAccount.getAccountId();
    }

    @Override
    public void deposit(UUID accountId, BigDecimal amount) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero.");
        }

        Account account = getAccount(accountId);

        account.deposit(amount);

        accountRepository.save(account);

        recordTransaction(account, TransactionType.DEPOSIT, amount);
    }

    @Override
    public void withdraw(UUID accountId, BigDecimal amount) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero.");
        }

        Account account = getAccount(accountId);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient balance.");
        }

        account.withdraw(amount);

        accountRepository.save(account);

        recordTransaction(account, TransactionType.WITHDRAWAL, amount);
    }

    @Override
    public void transfer(UUID fromAccountId,
                         UUID toAccountId,
                         BigDecimal amount) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero.");
        }

        if (fromAccountId.equals(toAccountId)) {
            throw new InvalidAmountException(
                    "Source and destination accounts cannot be the same.");
        }

        Account fromAccount = getAccount(fromAccountId);
        Account toAccount = getAccount(toAccountId);

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient balance.");
        }

        fromAccount.withdraw(amount);
        toAccount.deposit(amount);

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        recordTransaction(fromAccount, TransactionType.TRANSFER_OUT, amount);
        recordTransaction(toAccount, TransactionType.TRANSFER_IN, amount);
    }

    @Override
    public BigDecimal getBalance(UUID accountId) {

        Account account = getAccount(accountId);

        return account.getBalance();
    }

    @Override
    public List<TransactionResponse> getTransactions(UUID accountId) {

        Account account = getAccount(accountId);

        return transactionRepository
                .findByAccountOrderByTimestampDesc(account)
                .stream()
                .map(transaction -> new TransactionResponse(
                        transaction.getTransactionId(),
                        transaction.getTransactionType(),
                        transaction.getAmount(),
                        transaction.getTimestamp()))
                .toList();
    }

    private Account getAccount(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new AccountNotFoundException("Account not found."));
    }

    private void recordTransaction(Account account,
                                   TransactionType type,
                                   BigDecimal amount) {

        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionType(type)
                .amount(amount)
                .timestamp(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);
    }
}