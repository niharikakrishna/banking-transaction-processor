package com.example.banking.controller;

import com.example.banking.dto.AmountRequest;
import com.example.banking.dto.CreateAccountResponse;
import com.example.banking.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Void> deposit(
            @PathVariable UUID accountId,
            @Valid @RequestBody AmountRequest request) {

        accountService.deposit(accountId, request.amount());

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{accountId}/withdraw")
    public ResponseEntity<Void> withdraw(
            @PathVariable UUID accountId,
            @Valid @RequestBody AmountRequest request) {

        accountService.withdraw(accountId, request.amount());

        return ResponseEntity.noContent().build();
    }
}