package com.sports.auth.service;

import com.sports.auth.dto.AuthResponse;
import com.sports.auth.dto.LoginRequest;
import com.sports.auth.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
