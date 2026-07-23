package com.example.banking.controller;

import com.example.banking.dto.CreateAccountResponse;
import com.example.banking.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}