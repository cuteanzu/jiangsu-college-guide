package com.jiangsu.guide.common;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static CurrentUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        Long userId = (Long) auth.getPrincipal();
        String username = (String) auth.getDetails();
        String role = auth.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .map(r -> r.replace("ROLE_", ""))
                .orElse("USER");
        return new CurrentUser(userId, username, role);
    }

    public static Long getCurrentUserId() {
        CurrentUser user = getCurrentUser();
        return user != null ? user.getId() : null;
    }
}
