package com.example.banking.dto;

import java.math.BigDecimal;

public record BalanceResponse(
        BigDecimal balance
) {
}