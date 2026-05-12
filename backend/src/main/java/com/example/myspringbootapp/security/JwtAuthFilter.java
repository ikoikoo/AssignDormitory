package com.example.myspringbootapp.security;

import com.example.myspringbootapp.service.UserDetailsServiceImpl; // 你的 UserDetailsService
import com.example.myspringbootapp.util.JwtUtil; // 你的 JWT 工具类
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils; // 导入 StringUtils
import org.springframework.web.filter.OncePerRequestFilter; // 确保每个请求只过滤一次

import java.io.IOException;

@Component // 让 Spring 管理这个过滤器
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        System.out.println(">>> JwtAuthFilter 开始处理请求: " + request.getMethod() + " " + request.getRequestURI());
        try {
            // 1. 从请求中获取 JWT Token
            String jwt = parseJwt(request);
            System.out.println("    解析到的 JWT: " + (jwt != null ? "存在" : "不存在")); // <--- 添加日志

            // 2. 如果 Token 存在且有效
            if (jwt != null && jwtUtil.validateJwtToken(jwt)) {
                // 3. 从 Token 中获取用户名
                String username = jwtUtil.getUserNameFromJwtToken(jwt);

                // 4. 使用 UserDetailsService 加载用户信息
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 5. 创建 Spring Security 的 Authentication 对象
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()); // 注意：密码设为 null，因为我们是用 token 认证的

                // 6. 设置请求的详细信息 (如 IP 地址、Session ID 等)
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 7. 将 Authentication 对象设置到 SecurityContextHolder 中
                // 这表示当前请求的用户已经通过认证
                SecurityContextHolder.getContext().setAuthentication(authentication);
                System.out.println("提取到的用户角色: " + SecurityContextHolder.getContext().getAuthentication().getAuthorities());

                logger.debug("为用户 '{}' 设置了 Security Context", username);
            }
        } catch (ExpiredJwtException e){
            logger.warn("JWT Token 已过期: {}", e.getMessage());
            // 可以选择在这里设置响应头或状态码，但通常让后续的 Spring Security 处理
            // response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            // response.getWriter().write("Token expired");
            // return; // 直接返回，不继续过滤器链
        } catch (Exception e) {
            logger.error("无法设置用户认证: {}", e.getMessage());
            // 打印更详细的堆栈信息以便调试
            logger.error("认证错误详情", e);
        }
        System.out.println(">>> JwtAuthFilter 即将继续过滤器链..."); // <--- 添加日志

        // 8. 继续执行过滤器链中的下一个过滤器
        filterChain.doFilter(request, response);
    }

    // 从 HttpServletRequest 中解析 JWT Token
    private String parseJwt(HttpServletRequest request) {
        // Token 通常放在 Authorization 请求头中，格式为 "Bearer <token>"
        String headerAuth = request.getHeader("Authorization");

        // 检查请求头是否存在且以 "Bearer " 开头
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            // 提取 "Bearer " 后面的 Token 字符串
            return headerAuth.substring(7); // "Bearer ".length() == 7
        }

        // 如果请求头不符合要求，返回 null
        return null;
    }
}
