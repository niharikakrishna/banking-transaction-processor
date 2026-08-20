package com.example.banking.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record DepositResponse(
        UUID accountId,
        BigDecimal newBalance,
        UUID transactionId
) {
}