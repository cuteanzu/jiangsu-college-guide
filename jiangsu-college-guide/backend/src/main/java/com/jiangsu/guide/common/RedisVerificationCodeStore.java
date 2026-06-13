package com.jiangsu.guide.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Redis 验证码存储（生产环境）。当 Redis 可用时自动启用，覆盖默认的内存实现。
 *
 * 启用方式：移除 application.yml 中对 Redis 的 exclude，并确保 Redis 服务可连接。
 */
@Slf4j
@Component
@Primary
@ConditionalOnBean(StringRedisTemplate.class)
public class RedisVerificationCodeStore implements VerificationCodeStore {

    private static final String PREFIX = "verify_code:";
    private static final long TTL_MINUTES = 5;

    private final StringRedisTemplate redis;

    public RedisVerificationCodeStore(StringRedisTemplate redis) {
        this.redis = redis;
        log.info("✔ Redis 验证码存储已启用");
    }

    @Override
    public String generate(String email) {
        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1000000));
        String key = PREFIX + email.toLowerCase();
        redis.opsForValue().set(key, code, TTL_MINUTES, TimeUnit.MINUTES);
        log.info("📧 [Redis模式] 验证码已生成: email={}, code={}", email, code);
        return code;
    }

    @Override
    public boolean verify(String email, String code) {
        String key = PREFIX + email.toLowerCase();
        String stored = redis.opsForValue().get(key);
        if (stored == null) {
            log.warn("验证失败: 邮箱 {} 的验证码不存在或已过期", email);
            return false;
        }
        boolean match = stored.equals(code);
        if (match) {
            redis.delete(key);  // 一次性使用
            log.info("✔ 验证码校验成功: {}", email);
        } else {
            log.warn("验证失败: 邮箱 {} 的验证码不匹配", email);
        }
        return match;
    }
}
