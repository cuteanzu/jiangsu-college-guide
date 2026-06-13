package com.jiangsu.guide.service.impl;

import com.jiangsu.guide.common.VerificationCodeStore;
import com.jiangsu.guide.dto.*;
import com.jiangsu.guide.entity.User;
import com.jiangsu.guide.exception.BusinessException;
import com.jiangsu.guide.repository.UserRepository;
import com.jiangsu.guide.security.JwtTokenProvider;
import com.jiangsu.guide.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final VerificationCodeStore codeStore;

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("══════════════════════════════════════");
        log.info("▶ 登录请求");
        log.info("  账号:   {}", request.getUsername());
        log.info("  方式:   {}", request.isEmail() ? "邮箱登录" : "用户名登录");
        log.info("  密码:   {}", request.getPassword());
        log.info("══════════════════════════════════════");

        // 根据输入含 @ 判断是邮箱还是用户名
        User user;
        if (request.isEmail()) {
            user = userRepository.findByEmail(request.getUsername())
                    .orElseThrow(() -> {
                        log.warn("✘ 登录失败: 邮箱不存在 — {}", request.getUsername());
                        return new BusinessException(401, "邮箱或密码错误");
                    });
        } else {
            user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> {
                        log.warn("✘ 登录失败: 用户不存在 — {}", request.getUsername());
                        return new BusinessException(401, "用户名或密码错误");
                    });
        }

        if (user.getStatus() == 0) {
            log.warn("✘ 登录失败: 账户已禁用 — {}", request.getUsername());
            throw new BusinessException(403, "账户已被禁用");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("✘ 登录失败: 密码错误 — {}", request.getUsername());
            throw new BusinessException(401, "用户名或密码错误");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername());

        log.info("✔ 登录成功: userId={}, username={}, role={}", user.getId(), user.getUsername(), user.getRole());
        log.info("  Token: {}...", token.substring(0, Math.min(40, token.length())));
        return AuthResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .role(user.getRole())
                .token(token)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        log.info("══════════════════════════════════════");
        log.info("▶ 注册请求");
        log.info("  用户名: {}", request.getUsername());
        log.info("  密码:   {}", request.getPassword());
        log.info("  昵称:   {}", request.getNickname());
        log.info("  邮箱:   {}", request.getEmail());
        log.info("══════════════════════════════════════");

        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("✘ 注册失败: 用户名已存在 — {}", request.getUsername());
            throw new BusinessException("用户名已存在");
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()
                && userRepository.existsByEmail(request.getEmail())) {
            log.warn("✘ 注册失败: 邮箱已被注册 — {}", request.getEmail());
            throw new BusinessException("该邮箱已被注册");
        }

        // 如果填写了邮箱，必须验证验证码
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (request.getCode() == null || request.getCode().isBlank()) {
                throw new BusinessException("请填写邮箱验证码");
            }
            if (!codeStore.verify(request.getEmail(), request.getCode())) {
                throw new BusinessException("验证码错误或已过期");
            }
            log.info("  ✔ 邮箱验证通过: {}", request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname() != null ? request.getNickname() : request.getUsername())
                .email(request.getEmail() != null && !request.getEmail().isBlank() ? request.getEmail() : null)
                .role("USER")
                .status(1)
                .build();

        user = userRepository.save(user);

        log.info("✔ 注册成功: userId={}, username={}, nickname={}", user.getId(), user.getUsername(), user.getNickname());

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername());

        return AuthResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .role(user.getRole())
                .token(token)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public AuthResponse getMe(Long userId) {
        log.info("▶ 获取用户信息: userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("✘ 用户不存在: userId={}", userId);
                    return new BusinessException(404, "用户不存在");
                });

        log.info("  返回: username={}, role={}", user.getUsername(), user.getRole());
        return AuthResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .role(user.getRole())
                .build();
    }

    @Override
    public void logout(Long userId) {
        log.info("▶ 用户登出: userId={}", userId);
        // 使用JWT无状态认证，客户端删除token即可
        // 后续可接入Redis黑名单
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        log.info("▶ 修改密码: userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            log.warn("✘ 修改密码失败: 旧密码错误 — userId={}", userId);
            throw new BusinessException("旧密码不正确");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("✔ 密码修改成功: userId={}", userId);
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        log.info("▶ 重置密码: email={}", request.getEmail().toLowerCase());
        String email = request.getEmail().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("✘ 重置密码失败: 邮箱未注册 — {}", email);
                    return new BusinessException("该邮箱未注册");
                });

        if (!codeStore.verify(email, request.getCode())) {
            throw new BusinessException("验证码错误或已过期");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("✔ 密码重置成功: userId={}, email={}", user.getId(), email);
    }

    @Override
    public AuthResponse updateProfile(Long userId, UpdateProfileRequest request) {
        log.info("▶ 更新用户资料: userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        if (request.getNickname() != null && !request.getNickname().isBlank()) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }

        user = userRepository.save(user);
        log.info("✔ 用户资料已更新: userId={}, nickname={}", user.getId(), user.getNickname());

        return AuthResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .role(user.getRole())
                .build();
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.info("▶ 刷新 Token");
        String refreshToken = request.getRefreshToken();

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            log.warn("✘ Refresh token 无效或已过期");
            throw new BusinessException(401, "refreshToken 无效或已过期，请重新登录");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("✘ Token 刷新失败: 用户不存在 — userId={}", userId);
                    return new BusinessException(401, "用户不存在");
                });

        if (user.getStatus() == 0) {
            throw new BusinessException(403, "账户已被禁用");
        }

        String newToken = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername());

        log.info("✔ Token 刷新成功: userId={}, username={}", user.getId(), user.getUsername());
        return AuthResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .role(user.getRole())
                .token(newToken)
                .refreshToken(newRefreshToken)
                .build();
    }
}
