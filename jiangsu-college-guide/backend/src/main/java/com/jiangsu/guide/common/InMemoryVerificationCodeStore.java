package com.jiangsu.guide.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 内存验证码存储（开发环境默认实现）。
 * 当 Redis 不可用时自动启用。
 */
@Slf4j
@Component
public class InMemoryVerificationCodeStore implements VerificationCodeStore {

    /** email -> { code, expireTime } */
    private final Map<String, CodeEntry> store = new ConcurrentHashMap<>();

    /** 验证码有效期：5 分钟 */
    private static final long TTL_MS = 5 * 60 * 1000;

    /** 生成 6 位数字验证码 */
    @Override
    public String generate(String email) {
        cleanExpired();
        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1000000));
        store.put(email.toLowerCase(), new CodeEntry(code, System.currentTimeMillis() + TTL_MS));
        log.info("══════════════════════════════════════");
        log.info("📧 [内存模式] 验证码已生成");
        log.info("  邮箱: {}", email);
        log.info("  验证码: {}", code);
        log.info("  有效期: 5 分钟");
        log.info("══════════════════════════════════════");
        return code;
    }

    /** 验证码是否正确且未过期 */
    @Override
    public boolean verify(String email, String code) {
        cleanExpired();
        CodeEntry entry = store.get(email.toLowerCase());
        if (entry == null) {
            log.warn("验证失败: 邮箱 {} 没有发送过验证码", email);
            return false;
        }
        if (System.currentTimeMillis() > entry.expireTime) {
            log.warn("验证失败: 邮箱 {} 的验证码已过期", email);
            store.remove(email.toLowerCase());
            return false;
        }
        boolean match = entry.code.equals(code);
        if (match) {
            log.info("✔ 验证码校验成功: {}", email);
            store.remove(email.toLowerCase());  // 一次性使用
        } else {
            log.warn("验证失败: 邮箱 {} 的验证码不匹配 (expected={}, actual={})", email, entry.code, code);
        }
        return match;
    }

    private void cleanExpired() {
        long now = System.currentTimeMillis();
        store.entrySet().removeIf(e -> now > e.getValue().expireTime);
    }

    private record CodeEntry(String code, long expireTime) {}
}
