package com.example.service;

import com.example.dto.AuthUserDto;
import com.example.dto.ChangePasswordRequest;
import com.example.dto.LoginRequest;
import com.example.dto.LoginResponse;
import com.example.dto.ProfileUpdateRequest;
import com.example.dto.RegisterRequest;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    AuthUserDto register(RegisterRequest request);

    AuthUserDto getCurrentUser(Long userId);

    void changePassword(Long userId, ChangePasswordRequest request);

    AuthUserDto updateProfile(Long userId, ProfileUpdateRequest request);
}
