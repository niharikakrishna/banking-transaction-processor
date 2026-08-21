package com.example.banking.repository;

import com.example.banking.entity.Account;
import com.example.banking.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Page<Transaction> findByAccountOrderByTimestampDesc(
            Account account,
            Pageable pageable
    );

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

}