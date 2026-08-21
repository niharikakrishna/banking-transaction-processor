package com.example.banking.controller;

import com.example.banking.dto.*;
import com.example.banking.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public CreateAccountResponse createAccount() {

        return new CreateAccountResponse(
                accountService.createAccount()
        );
    }

    @PostMapping("/{accountId}/deposit")
    public ResponseEntity<DepositResponse> deposit(
            @PathVariable UUID accountId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody AmountRequest request) {

        DepositResponse response = accountService.deposit(
                accountId,
                request.amount(),
                idempotencyKey
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{accountId}/withdraw")
    public ResponseEntity<Void> withdraw(
            @PathVariable UUID accountId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody AmountRequest request) {

        accountService.withdraw(accountId, request.amount(), idempotencyKey);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(
            @Valid @RequestBody TransferRequest request) {

        accountService.transfer(
                request.fromAccount(),
                request.toAccount(),
                request.amount());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{accountId}/balance")
    public ResponseEntity<BalanceResponse> getBalance(
            @PathVariable UUID accountId) {

        BigDecimal balance = accountService.getBalance(accountId);

        return ResponseEntity.ok(new BalanceResponse(balance));
    }

    @GetMapping("/{accountId}/transactions")
    public ResponseEntity<Page<TransactionResponse>> getTransactions(
            @PathVariable UUID accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                accountService.getTransactions(
                        accountId,
                        page,
                        size)
        );
    }
}