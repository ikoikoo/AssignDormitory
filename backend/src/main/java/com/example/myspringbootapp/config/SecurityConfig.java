package com.example.myspringbootapp.config;

import com.example.myspringbootapp.security.JwtAuthFilter; // 需要创建 JWT 过滤器
import com.example.myspringbootapp.service.UserDetailsServiceImpl; // 需要创建 UserDetailsService 实现
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // 启用方法级别安全 (可选)
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;         // 导入 CORS 相关类
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;
import java.util.Arrays;

@Configuration
@EnableWebSecurity // 启用 Spring Security 的 Web 安全支持
//@EnableMethodSecurity // 启用基于注解的方法级别安全控制 (如 @PreAuthorize)
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService; // 自定义的 UserDetailsService

    @Autowired
    private JwtAuthFilter jwtAuthFilter; // 自定义的 JWT 过滤器

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean // 暴露 AuthenticationManager Bean，供 AuthService 使用
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean // 定义 CORS 配置源
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 允许来自前端开发服务器的源
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        // 允许所有常用的 HTTP 方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        // 允许所有请求头
        configuration.setAllowedHeaders(Arrays.asList("*")); // 或者更具体地列出如 "Authorization", "Content-Type"
        // 允许发送凭证 (如 Cookies, Authorization header)，如果前端需要的话
        configuration.setAllowCredentials(true);
        // 预检请求 (OPTIONS) 的缓存时间
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有路径应用此 CORS 配置
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable()) // 禁用 CSRF (JWT 无状态)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 无状态 Session
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/announcements").authenticated()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // --- 添加明确规则 ---
                        // 允许管理员访问所有 /api/users 下的路径
                        //.requestMatchers("/api/users/**").hasRole("ADMIN")
                        // 允许普通用户访问自己的 profile (这个用 SpEL 可能更复杂，不如放在 Controller 层)
                        // .requestMatchers("/api/users/{userId}/profile").access(...)
                        .anyRequest().authenticated() // 其他所有请求都需要认证
                )
                // 在处理用户名密码认证前，先用 JWT 过滤器进行验证
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
