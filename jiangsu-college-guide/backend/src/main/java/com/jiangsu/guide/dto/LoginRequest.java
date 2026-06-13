package com.jiangsu.guide.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "用户名/邮箱不能为空")
    private String username;  // 可以是用户名或邮箱，含 @ 则按邮箱查找

    @NotBlank(message = "密码不能为空")
    private String password;

    /** 判断输入是否为邮箱格式 */
    public boolean isEmail() {
        return username != null && username.contains("@");
    }
}
