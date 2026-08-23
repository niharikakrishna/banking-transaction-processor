package com.example.banking.controller;

import com.example.banking.dto.AuthResponse;
import com.example.banking.dto.CreateUserRequest;
import com.example.banking.dto.LoginRequest;
import com.example.banking.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    @PostMapping
    public ResponseEntity<Void> createUser(
            @Valid @RequestBody CreateUserRequest request) {

        authService.createUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }
}