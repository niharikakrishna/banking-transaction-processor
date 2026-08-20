package com.example.banking.service;

import com.example.banking.dto.DepositResponse;
import com.example.banking.dto.TransactionResponse;
import com.example.banking.entity.Account;
import com.example.banking.entity.IdempotencyRecord;
import com.example.banking.entity.Transaction;
import com.example.banking.enums.TransactionType;
import com.example.banking.exception.AccountNotFoundException;
import com.example.banking.exception.IdempotencyKeyReuseException;
import com.example.banking.exception.InsufficientFundsException;
import com.example.banking.exception.InvalidAmountException;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.IdempotencyRecordRepository;
import com.example.banking.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final IdempotencyRecordRepository idempotencyRecordRepository;

    @Override
    public UUID createAccount() {

        Account account = Account.builder()
                .balance(BigDecimal.ZERO)
                .build();

        Account savedAccount = accountRepository.save(account);

        return savedAccount.getAccountId();
    }

    @Override
    @Transactional
    public DepositResponse deposit(
            UUID accountId,
            BigDecimal amount,
            String idempotencyKey) {

        // 1. Validate amount
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException(
                    "Amount must be greater than zero."
            );
        }

        // 2. Check whether this request was already processed
        Optional<IdempotencyRecord> existingRecord =
                idempotencyRecordRepository
                        .findByIdempotencyKey(idempotencyKey);

        if (existingRecord.isPresent()) {

            IdempotencyRecord record = existingRecord.get();

            // Same idempotency key must represent the same request
            if (!record.getAccountId().equals(accountId)
                    || record.getAmount().compareTo(amount) != 0
                    || record.getTransactionType()
                    != TransactionType.DEPOSIT) {

                throw new IdempotencyKeyReuseException(
                        "Idempotency key was already used for a different request."
                );
            }

            // Request was already successfully processed.
            // Return the original result.
            return new DepositResponse(
                    record.getAccountId(),
                    record.getNewBalance(),
                    record.getTransactionId()
            );
        }

        // 3. Get the account
        Account account = getAccount(accountId);

        // 4. Perform the deposit
        account.deposit(amount);

        accountRepository.save(account);

        // 5. Record the transaction
        Transaction transaction = recordTransaction(
                account,
                TransactionType.DEPOSIT,
                amount
        );

        // 6. Store idempotency information
        IdempotencyRecord idempotencyRecord =
                new IdempotencyRecord(
                        idempotencyKey,
                        accountId,
                        amount,
                        TransactionType.DEPOSIT,
                        transaction.getTransactionId(),
                        account.getBalance()
                );

        idempotencyRecordRepository.save(idempotencyRecord);

        // 7. Return response
        return new DepositResponse(
                accountId,
                account.getBalance(),
                transaction.getTransactionId()
        );
    }

    @Override
    @Transactional
    public void withdraw(
            UUID accountId,
            BigDecimal amount,
            String idempotencyKey) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException(
                    "Amount must be greater than zero."
            );
        }

        // Check if this request was already processed
        if (transactionRepository
                .findByIdempotencyKey(idempotencyKey)
                .isPresent()) {

            return;
        }

        Account account = getAccount(accountId);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient balance.");
        }

        account.withdraw(amount);

        accountRepository.save(account);

        recordTransaction(
                account,
                TransactionType.WITHDRAWAL,
                amount,
                idempotencyKey
        );
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

    private Transaction recordTransaction(Account account,
                                   TransactionType type,
                                   BigDecimal amount) {

        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionType(type)
                .amount(amount)
                .timestamp(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);
        return transaction;
    }

    private Transaction recordTransaction(
            Account account,
            TransactionType transactionType,
            BigDecimal amount,
            String idempotencyKey) {

        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionType(transactionType)
                .amount(amount)
                .idempotencyKey(idempotencyKey)
                .timestamp(LocalDateTime.now())
                .build();

        return transactionRepository.save(transaction);
    }
}