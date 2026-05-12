package com.example.myspringbootapp.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException; // 导入正确的 SignatureException
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails; // 导入 UserDetails
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${app.jwt.secret}") // 从 application.properties 读取密钥
    private String jwtSecret;

    @Value("${app.jwt.expirationMs}") // 从 application.properties 读取过期时间 (毫秒)
    private int jwtExpirationMs;

    private Key key;

    // 在构造函数或 @PostConstruct 中初始化 Key
    @jakarta.annotation.PostConstruct // 确保在依赖注入后执行
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // 生成 JWT
    public String generateJwtToken(Authentication authentication) {
        // Spring Security 的 Authentication 对象中获取 Principal (通常是 UserDetails)
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        return generateTokenFromUsername(userPrincipal.getUsername());
    }

    // 从用户名生成 Token (方便注册后直接生成或刷新 token 时使用)
    public String generateTokenFromUsername(String username) {
        return Jwts.builder()
                .setSubject(username) // 将用户名作为 subject
                .setIssuedAt(new Date()) // 设置签发时间
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs)) // 设置过期时间
                .signWith(key, SignatureAlgorithm.HS512) // 使用 HS512 算法和密钥签名
                .compact(); // 生成字符串 Token
    }

    // 从 JWT 中获取用户名
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    // 验证 JWT
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            logger.error("无效的 JWT 签名: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("无效的 JWT Token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT Token 已过期: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("不支持的 JWT Token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims 字符串为空: {}", e.getMessage());
        }
        return false;
    }
}
