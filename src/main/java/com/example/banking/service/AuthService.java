package com.example.banking.service;

import com.example.banking.dto.AuthResponse;
import com.example.banking.dto.CreateUserRequest;
import com.example.banking.dto.LoginRequest;
import com.example.banking.dto.RegisterRequest;

public interface AuthService {

    void register(RegisterRequest request);

    void createUser(CreateUserRequest request);

    AuthResponse login(LoginRequest request);
}