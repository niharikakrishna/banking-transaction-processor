package com.example.banking.entity;

import com.example.banking.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
        name = "idempotency_records",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_idempotency_key",
                columnNames = "idempotency_key"
        )
)
@Getter
@Setter
@NoArgsConstructor
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(nullable = false)
    private UUID accountId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @Column(nullable = false)
    private UUID transactionId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal newBalance;

    public IdempotencyRecord(
            String idempotencyKey,
            UUID accountId,
            BigDecimal amount,
            TransactionType transactionType,
            UUID transactionId,
            BigDecimal newBalance) {

        this.idempotencyKey = idempotencyKey;
        this.accountId = accountId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.transactionId = transactionId;
        this.newBalance = newBalance;
    }
}