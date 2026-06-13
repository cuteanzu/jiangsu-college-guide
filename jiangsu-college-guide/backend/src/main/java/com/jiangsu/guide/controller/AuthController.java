package com.jiangsu.guide.controller;

import com.jiangsu.guide.common.Result;
import com.jiangsu.guide.common.SecurityUtils;
import com.jiangsu.guide.common.VerificationCodeStore;
import com.jiangsu.guide.dto.*;
import com.jiangsu.guide.exception.BusinessException;
import com.jiangsu.guide.service.AuthService;
import com.jiangsu.guide.service.MailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final VerificationCodeStore codeStore;
    private final MailService mailService;

    @PostMapping("/login")
    public Result<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    @PostMapping("/register")
    public Result<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.ok(authService.register(request));
    }

    @PostMapping("/send-code")
    public Result<String> sendCode(@Valid @RequestBody SendCodeRequest request) {
        String email = request.getEmail().toLowerCase();
        // 生成验证码
        String code = codeStore.generate(email);
        // 发送邮件（或开发环境打印控制台）
        mailService.sendVerificationCode(email, code);
        return Result.ok("验证码已发送至 " + email + "，5 分钟内有效。开发环境请查看控制台日志。");
    }

    @GetMapping("/me")
    public Result<AuthResponse> me() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.ok(authService.getMe(userId));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        Long userId = SecurityUtils.getCurrentUserId();
        authService.logout(userId);
        return Result.ok(null);
    }

    @PostMapping("/change-password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        authService.changePassword(userId, request);
        return Result.ok(null);
    }

    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return Result.ok(null);
    }

    @PutMapping("/profile")
    public Result<AuthResponse> updateProfile(@RequestBody UpdateProfileRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.ok(authService.updateProfile(userId, request));
    }

    @PostMapping("/refresh")
    public Result<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return Result.ok(authService.refreshToken(request));
    }
}
