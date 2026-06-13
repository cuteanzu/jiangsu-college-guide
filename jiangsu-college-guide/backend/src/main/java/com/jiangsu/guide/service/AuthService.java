package com.jiangsu.guide.service;

import com.jiangsu.guide.dto.*;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
    AuthResponse getMe(Long userId);
    void logout(Long userId);
    void changePassword(Long userId, ChangePasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
    AuthResponse updateProfile(Long userId, UpdateProfileRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
}
