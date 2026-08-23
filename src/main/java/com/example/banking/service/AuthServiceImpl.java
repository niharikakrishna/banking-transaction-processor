package com.example.banking.service;

import com.example.banking.dto.AuthResponse;
import com.example.banking.dto.CreateUserRequest;
import com.example.banking.dto.LoginRequest;
import com.example.banking.dto.RegisterRequest;
import com.example.banking.entity.User;
import com.example.banking.enums.Role;
import com.example.banking.repository.UserRepository;
import com.example.banking.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    public void register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException(
                    "Username already exists"
            );
        }

        User user = User.builder()
                .username(request.username())
                .password(
                        passwordEncoder.encode(request.password())
                )
                .role(Role.CUSTOMER)
                .build();

        userRepository.save(user);
    }

    @Override
    public void createUser(CreateUserRequest request) {

        if (request.role() == Role.ADMIN) {
            throw new IllegalArgumentException(
                    "ADMIN users cannot be created through this endpoint"
            );
        }

        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException(
                    "Username already exists"
            );
        }

        User user = User.builder()
                .username(request.username())
                .password(
                        passwordEncoder.encode(request.password())
                )
                .role(request.role())
                .build();

        userRepository.save(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        User user = userRepository
                .findByUsername(request.username())
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found")
                );

        String token = jwtService.generateToken(user.getUsername());

        return new AuthResponse(token);
    }
}