package com.jiangsu.guide.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;  // 未配置 SMTP 时为 null

    @Value("${spring.mail.username:}")
    private String mailFrom;

    /**
     * 发送验证码邮件。如果 SMTP 未配置（spring.mail.host 为空），
     * 验证码只打印到控制台，不真实发送。
     */
    public void sendVerificationCode(String toEmail, String code) {
        // 检查 SMTP 是否配置
        if (mailSender == null || mailFrom == null || mailFrom.isBlank()) {
            log.info("📧 [DEV模式] SMTP 未配置，验证码已打印到控制台（见上方日志）");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(toEmail);
            helper.setSubject("江苏高校生活指北 - 邮箱验证码");
            helper.setText(buildEmailContent(code), true);

            mailSender.send(message);
            log.info("✔ 验证码邮件已发送至: {}", toEmail);
        } catch (MessagingException e) {
            log.error("✘ 邮件发送失败: {}", e.getMessage());
            throw new RuntimeException("邮件发送失败，请稍后重试");
        }
    }

    private String buildEmailContent(String code) {
        return """
            <div style="max-width:480px;margin:0 auto;padding:32px;font-family:sans-serif;
                        background:#fdf7f2;border-radius:12px;border:1px solid #e8d5c4;">
              <h2 style="color:#3a2f28;margin-top:0;">📮 江苏高校生活指北</h2>
              <p style="color:#5a4a3a;">您好！您正在注册账号，验证码如下：</p>
              <div style="text-align:center;margin:24px 0;">
                <span style="font-size:32px;font-weight:800;letter-spacing:6px;color:#c76b5e;
                             background:#fff;padding:12px 28px;border-radius:8px;border:1px solid #e8d5c4;">
                  %s
                </span>
              </div>
              <p style="color:#8b7b6a;font-size:13px;">验证码 5 分钟内有效，请勿泄露给他人。</p>
              <hr style="border:none;border-top:1px solid #e8d5c4;margin:20px 0;">
              <p style="color:#b0a090;font-size:11px;">如果这不是您的操作，请忽略此邮件。</p>
            </div>
            """.formatted(code);
    }
}
