package com.example.banking.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void shouldHandleAccountNotFoundException() {

        AccountNotFoundException exception =
                new AccountNotFoundException("Account not found.");

        ResponseEntity<String> response =
                exceptionHandler.handleAccountNotFoundException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Account not found.", response.getBody());
    }

    @Test
    void shouldHandleInsufficientFundsException() {

        InsufficientFundsException exception =
                new InsufficientFundsException("Insufficient balance.");

        ResponseEntity<String> response =
                exceptionHandler.handleInsufficientFundsException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Insufficient balance.", response.getBody());
    }

    @Test
    void shouldHandleInvalidAmountException() {

        InvalidAmountException exception =
                new InvalidAmountException("Amount must be greater than zero.");

        ResponseEntity<String> response =
                exceptionHandler.handleInvalidAmountException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Amount must be greater than zero.", response.getBody());
    }

    @Test
    void shouldHandleGenericException() {

        Exception exception = new Exception("Unexpected error");

        ResponseEntity<String> response =
                exceptionHandler.handleGenericException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Something went wrong!", response.getBody());
    }
}