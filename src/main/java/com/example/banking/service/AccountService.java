package com.example.banking.service;

import com.example.banking.dto.TransactionResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface AccountService {

    UUID createAccount();

    void deposit(UUID accountId, BigDecimal amount);

    void withdraw(UUID accountId, BigDecimal amount);

    void transfer(UUID fromAccount, UUID toAccount, BigDecimal amount);

    BigDecimal getBalance(UUID accountId);

    List<TransactionResponse> getTransactions(UUID accountId);

}