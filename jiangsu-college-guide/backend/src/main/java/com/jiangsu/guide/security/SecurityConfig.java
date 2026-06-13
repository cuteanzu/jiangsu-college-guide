package com.jiangsu.guide.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // CORS 预检请求全部放行
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // 公开接口
                .requestMatchers(HttpMethod.GET,
                        "/api/cities/**",
                        "/api/schools/**",
                        "/api/comments/**",
                        "/api/experiences/**",
                        "/api/qa/**").permitAll()
                .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/send-code", "/api/auth/reset-password", "/api/auth/refresh").permitAll()
                // 上传文件
                .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                // Swagger & H2
                .requestMatchers(
                        "/api/docs/**", "/api/docs",
                        "/api/api-docs/**", "/api/api-docs",
                        "/api/swagger-ui/**", "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/h2-console/**"
                ).permitAll()
                // 管理后台需要ADMIN角色
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // 其他需要登录
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            // H2 console 需要允许 frame
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:5173", "http://localhost:5174", "http://localhost:5175",
                "http://localhost:3000", "http://localhost:3001",
                "http://127.0.0.1:5173", "http://127.0.0.1:5174",
                "http://10.102.54.218:5173", "http://10.102.54.218:5174"
        ));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
