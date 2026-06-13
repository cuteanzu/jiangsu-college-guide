package com.jiangsu.guide;

import com.jiangsu.guide.common.InMemoryVerificationCodeStore;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 基础单元测试 — 不加载 Spring 上下文，快速验证核心逻辑。
 */
class GuideApplicationTests {

    @Test
    void verificationCodeStore_generateAndVerify() {
        InMemoryVerificationCodeStore store = new InMemoryVerificationCodeStore();
        String code = store.generate("test@test.com");
        assertNotNull(code);
        assertEquals(6, code.length());
        assertTrue(code.matches("\\d{6}"));
        assertTrue(store.verify("test@test.com", code));
        assertFalse(store.verify("test@test.com", code)); // 一次性使用
    }

    @Test
    void verificationCodeStore_caseInsensitive() {
        InMemoryVerificationCodeStore store = new InMemoryVerificationCodeStore();
        String code = store.generate("Test@Example.COM");
        assertTrue(store.verify("test@example.com", code));
    }
}
