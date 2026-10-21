package com.receiptmanager.service;

import com.receiptmanager.dto.AuthDtos.AuthResponse;
import com.receiptmanager.dto.AuthDtos.LoginRequest;
import com.receiptmanager.dto.AuthDtos.RegisterRequest;
import com.receiptmanager.entity.User;
import com.receiptmanager.exception.ApiException;
import com.receiptmanager.repository.UserRepository;
import com.receiptmanager.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ApiException(HttpStatus.CONFLICT, "Username already taken");
        }
        User user = User.builder()
                .username(request.username())
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();
        userRepository.save(user);
        return new AuthResponse(jwtService.generateToken(user.getUsername()), user.getUsername());
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (BadCredentialsException ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }
        return new AuthResponse(jwtService.generateToken(request.username()), request.username());
    }
}
