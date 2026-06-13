package com.jiangsu.guide.service;

import com.jiangsu.guide.common.InMemoryVerificationCodeStore;
import com.jiangsu.guide.dto.*;
import com.jiangsu.guide.entity.User;
import com.jiangsu.guide.exception.BusinessException;
import com.jiangsu.guide.repository.UserRepository;
import com.jiangsu.guide.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final InMemoryVerificationCodeStore codeStore = new InMemoryVerificationCodeStore();

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new com.jiangsu.guide.service.impl.AuthServiceImpl(
                userRepository, passwordEncoder, jwtTokenProvider, codeStore);
    }

    @Test
    void register_shouldSucceed_withValidInput() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);

        User savedUser = User.builder()
                .id(1L)
                .username("testuser")
                .nickname("Test")
                .password("encoded")
                .role("USER")
                .status(1)
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtTokenProvider.generateToken(1L, "testuser", "USER")).thenReturn("token");
        when(jwtTokenProvider.generateRefreshToken(1L, "testuser")).thenReturn("refresh");

        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("123456");
        request.setNickname("Test");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertEquals("token", response.getToken());
        assertEquals("refresh", response.getRefreshToken());
    }

    @Test
    void register_shouldFail_whenUsernameExists() {
        when(userRepository.existsByUsername("existing")).thenReturn(true);

        RegisterRequest request = new RegisterRequest();
        request.setUsername("existing");
        request.setPassword("123456");

        assertThrows(BusinessException.class, () -> authService.register(request));
    }

    @Test
    void login_shouldSucceed_withCorrectPassword() {
        String rawPassword = "123456";
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .password(passwordEncoder.encode(rawPassword))
                .role("USER")
                .status(1)
                .build();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken(1L, "testuser", "USER")).thenReturn("token");
        when(jwtTokenProvider.generateRefreshToken(1L, "testuser")).thenReturn("refresh");

        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword(rawPassword);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
    }

    @Test
    void login_shouldFail_withWrongPassword() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .password(passwordEncoder.encode("correct"))
                .role("USER")
                .status(1)
                .build();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrong");

        assertThrows(BusinessException.class, () -> authService.login(request));
    }

    @Test
    void changePassword_shouldSucceed_withCorrectOldPassword() {
        String oldPwd = "oldpass";
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .password(passwordEncoder.encode(oldPwd))
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword(oldPwd);
        request.setNewPassword("newpass");

        assertDoesNotThrow(() -> authService.changePassword(1L, request));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void changePassword_shouldFail_withWrongOldPassword() {
        User user = User.builder()
                .id(1L)
                .password(passwordEncoder.encode("correct"))
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("wrong");
        request.setNewPassword("newpass");

        assertThrows(BusinessException.class, () -> authService.changePassword(1L, request));
    }

    @Test
    void updateProfile_shouldUpdateNickname() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .nickname("Old")
                .role("USER")
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setNickname("NewNickname");

        AuthResponse response = authService.updateProfile(1L, request);

        assertEquals("NewNickname", response.getNickname());
    }
}
