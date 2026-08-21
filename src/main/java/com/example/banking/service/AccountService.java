package com.example.banking.service;

import com.example.banking.dto.DepositResponse;
import com.example.banking.dto.TransactionResponse;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.UUID;

public interface AccountService {

    UUID createAccount();

    DepositResponse deposit(UUID accountId, BigDecimal amount, String idempotencyKey);

    void withdraw(UUID accountId, BigDecimal amount, String idempotencyKey);

    void transfer(UUID fromAccount, UUID toAccount, BigDecimal amount);

    BigDecimal getBalance(UUID accountId);

    Page<TransactionResponse> getTransactions(UUID accountId, int page, int size);

}