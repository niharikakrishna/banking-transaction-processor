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
    public void transfer(UUID fromAccount, UUID toAccount, BigDecimal amount) {
    }

    @Override
    public BigDecimal getBalance(UUID accountId) {
        return null;
    }

    @Override
    public List<TransactionResponse> getTransactions(UUID accountId) {
        return null;
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