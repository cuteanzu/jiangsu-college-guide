package com.jiangsu.guide.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CurrentUser {
    private Long id;
    private String username;
    private String role;

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
