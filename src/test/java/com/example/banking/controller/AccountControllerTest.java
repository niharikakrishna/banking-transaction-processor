package com.example.banking.controller;

import com.example.banking.dto.*;
import com.example.banking.enums.TransactionType;
import com.example.banking.service.AccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountController accountController;

    @Test
    void shouldCreateAccount() {

        UUID accountId = UUID.randomUUID();

        when(accountService.createAccount()).thenReturn(accountId);

        CreateAccountResponse response = accountController.createAccount();

        assertNotNull(response);
        assertEquals(accountId, response.accountId());

        verify(accountService).createAccount();
    }

    @Test
    void shouldDeposit() {

        String idempotencyKey = "xyz";
        UUID accountId = UUID.randomUUID();
        AmountRequest request = new AmountRequest(BigDecimal.valueOf(500));

        ResponseEntity<DepositResponse> response =
                accountController.deposit(accountId, idempotencyKey, request);

        assertEquals(200, response.getStatusCode().value());

        verify(accountService)
                .deposit(accountId, BigDecimal.valueOf(500), idempotencyKey);
    }

    @Test
    void shouldWithdraw() {

        String idempotencyKey = "xyz";
        UUID accountId = UUID.randomUUID();
        AmountRequest request = new AmountRequest(BigDecimal.valueOf(250));

        ResponseEntity<Void> response =
                accountController.withdraw(accountId, idempotencyKey, request);

        assertEquals(204, response.getStatusCode().value());

        verify(accountService)
                .withdraw(accountId, BigDecimal.valueOf(250), idempotencyKey);
    }

    @Test
    void shouldTransfer() {

        UUID from = UUID.randomUUID();
        UUID to = UUID.randomUUID();

        TransferRequest request = new TransferRequest(
                from,
                to,
                BigDecimal.valueOf(300)
        );

        ResponseEntity<Void> response =
                accountController.transfer(request);

        assertEquals(204, response.getStatusCode().value());

        verify(accountService)
                .transfer(from, to, BigDecimal.valueOf(300));
    }

    @Test
    void shouldReturnBalance() {

        UUID accountId = UUID.randomUUID();

        when(accountService.getBalance(accountId))
                .thenReturn(BigDecimal.valueOf(1500));

        ResponseEntity<BalanceResponse> response =
                accountController.getBalance(accountId);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(
                BigDecimal.valueOf(1500),
                response.getBody().balance()
        );

        verify(accountService).getBalance(accountId);
    }

    @Test
    void shouldReturnTransactions() {

        UUID accountId = UUID.randomUUID();

        TransactionResponse transaction =
                new TransactionResponse(
                        UUID.randomUUID(),
                        TransactionType.DEPOSIT,
                        BigDecimal.valueOf(500),
                        LocalDateTime.now()
                );

        Page<TransactionResponse> transactionPage =
                new PageImpl<>(List.of(transaction));

        when(accountService.getTransactions(accountId, 0, 10))
                .thenReturn(transactionPage);

        ResponseEntity<Page<TransactionResponse>> response =
                accountController.getTransactions(accountId, 0, 10);

        assertEquals(200, response.getStatusCode().value());

        assertNotNull(response.getBody());

        assertEquals(1, response.getBody().getContent().size());

        TransactionResponse result =
                response.getBody()
                        .getContent()
                        .getFirst();

        assertEquals(
                TransactionType.DEPOSIT,
                result.transactionType()
        );

        assertEquals(
                BigDecimal.valueOf(500),
                result.amount()
        );

        verify(accountService)
                .getTransactions(accountId, 0, 10);
    }
}