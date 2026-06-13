package com.jiangsu.guide.common;

/**
 * 验证码存储接口。开发环境使用内存实现，生产环境使用 Redis 实现。
 */
public interface VerificationCodeStore {
    String generate(String email);
    boolean verify(String email, String code);
}
