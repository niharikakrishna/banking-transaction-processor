package com.example.banking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(

        @NotNull
        UUID fromAccount,

        @NotNull
        UUID toAccount,

        @NotNull
        @Positive
        BigDecimal amount

) {
}