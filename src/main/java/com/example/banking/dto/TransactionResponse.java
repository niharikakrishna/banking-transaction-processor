package com.example.banking.dto;

import com.example.banking.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponse(

        UUID transactionId,
        TransactionType transactionType,
        BigDecimal amount,
        LocalDateTime timestamp

) {
}