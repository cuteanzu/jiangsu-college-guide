package com.jiangsu.guide.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VerificationCodeStoreTest {

    private InMemoryVerificationCodeStore codeStore;

    @BeforeEach
    void setUp() {
        codeStore = new InMemoryVerificationCodeStore();
    }

    @Test
    void generate_shouldReturn6DigitCode() {
        String code = codeStore.generate("test@example.com");
        assertNotNull(code);
        assertEquals(6, code.length());
        assertTrue(code.matches("\\d{6}"));
    }

    @Test
    void verify_shouldReturnTrue_withCorrectCode() {
        String code = codeStore.generate("test@example.com");
        assertTrue(codeStore.verify("test@example.com", code));
    }

    @Test
    void verify_shouldReturnFalse_withWrongCode() {
        codeStore.generate("test@example.com");
        assertFalse(codeStore.verify("test@example.com", "000000"));
    }

    @Test
    void verify_shouldReturnFalse_withNoCodeGenerated() {
        assertFalse(codeStore.verify("nonexistent@example.com", "123456"));
    }

    @Test
    void verify_shouldBeCaseInsensitive() {
        String code = codeStore.generate("Test@Example.com");
        assertTrue(codeStore.verify("test@example.com", code));
    }

    @Test
    void verify_shouldBeOneTimeUse() {
        String code = codeStore.generate("test@example.com");
        assertTrue(codeStore.verify("test@example.com", code));
        assertFalse(codeStore.verify("test@example.com", code)); // 第二次应失败
    }
}
