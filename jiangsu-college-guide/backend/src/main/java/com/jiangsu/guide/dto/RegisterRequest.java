package com.jiangsu.guide.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度3-50位")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度6-100位")
    private String password;

    @Size(max = 50)
    private String nickname;

    @Size(max = 100)
    private String email;  // 可选：注册时填写邮箱

    private String code;  // 邮箱验证码（填写了邮箱时必填）
}
