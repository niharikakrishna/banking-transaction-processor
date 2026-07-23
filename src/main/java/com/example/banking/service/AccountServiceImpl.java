package com.example.banking.service;

import com.example.banking.dto.TransactionResponse;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
        return null;
    }

    @Override
    public void deposit(UUID accountId, BigDecimal amount) {
    }

    @Override
    public void withdraw(UUID accountId, BigDecimal amount) {
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
}