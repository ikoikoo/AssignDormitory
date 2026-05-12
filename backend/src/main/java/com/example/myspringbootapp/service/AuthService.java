package com.example.myspringbootapp.service;

import com.example.myspringbootapp.dto.LoginRequestDto;
import com.example.myspringbootapp.dto.LoginResponseDto;
import com.example.myspringbootapp.dto.RegisterRequestDto;
import com.example.myspringbootapp.entity.User;
import com.example.myspringbootapp.exception.UsernameAlreadyExistsException; // 需要创建自定义异常
import com.example.myspringbootapp.exception.EmailAlreadyExistsException;   // 需要创建自定义异常
import com.example.myspringbootapp.repository.UserRepository;
import com.example.myspringbootapp.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager; // 注入 AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager; // 用于认证

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // 用于加密密码

    @Autowired
    private JwtUtil jwtUtil; // 用于生成 Token

    @Transactional
    public User registerUser(RegisterRequestDto registerRequestDto) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(registerRequestDto.getUsername())) {
            throw new UsernameAlreadyExistsException("错误: 用户名已被注册!");
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(registerRequestDto.getEmail())) {
            throw new EmailAlreadyExistsException("错误: 邮箱已被注册!");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(registerRequestDto.getUsername());
        user.setEmail(registerRequestDto.getEmail());
        // 加密密码
        user.setPassword(passwordEncoder.encode(registerRequestDto.getPassword()));
        userRepository.save(user);
        // --- 确保处理 name 和 studentId ---
        if (registerRequestDto.getName() != null && !registerRequestDto.getName().trim().isEmpty()) {
            user.setName(registerRequestDto.getName());
        }
        if (registerRequestDto.getStudentId() != null && !registerRequestDto.getStudentId().trim().isEmpty()) {
            user.setStudentId(registerRequestDto.getStudentId());
        }
        if (registerRequestDto.getGender() != null && !registerRequestDto.getGender().trim().isEmpty()) {
            user.setGender(registerRequestDto.getGender());
        }

        System.out.println("准备保存的用户信息: " + user); // 添加日志，查看即将保存的对象
        return userRepository.save(user);
    }

    public LoginResponseDto authenticateUser(LoginRequestDto loginRequestDto) {
        // 1. 使用 AuthenticationManager 进行认证
        // 它会调用 UserDetailsService 加载用户信息并比对密码
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getUsername(),
                        loginRequestDto.getPassword()
                )
        );

        // 2. 如果认证成功，将认证信息设置到 SecurityContextHolder
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. 生成 JWT Token
        String jwt = jwtUtil.generateJwtToken(authentication);

        // 4. 从认证信息中获取 UserDetails (包含了用户信息)
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // 5. 从数据库获取完整的 User 对象 (为了获取 ID 等额外信息)
        // 注意：这里假设 UserDetails 的 getUsername() 返回的是数据库中的 username
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("认证成功但未找到用户: " + userDetails.getUsername()));
        // --- 从认证信息中提取角色/权限 ---
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority) // 获取权限字符串 (例如 "ROLE_ADMIN")
                .collect(Collectors.toList());
        System.out.println("提取到的用户角色: " + roles); // <--- 添加日志确认！
        // 6. 构建并返回 LoginResponseDto
        return new LoginResponseDto(
                jwt,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                roles
        );
    }
}
