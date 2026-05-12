package com.example.myspringbootapp.controller;

import com.example.myspringbootapp.dto.LoginRequestDto;
import com.example.myspringbootapp.dto.LoginResponseDto;
import com.example.myspringbootapp.dto.RegisterRequestDto;
import com.example.myspringbootapp.entity.User;
import com.example.myspringbootapp.exception.EmailAlreadyExistsException;
import com.example.myspringbootapp.exception.UsernameAlreadyExistsException;
import com.example.myspringbootapp.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException; // 捕获认证异常
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth") // 认证相关的 API 路径
@CrossOrigin(origins = "http://localhost:5173") // 允许前端访问
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequestDto registerRequestDto) {
        try {
            User registeredUser = authService.registerUser(registerRequestDto);
            // 返回成功消息或注册后的用户信息(不含密码)
            return ResponseEntity.ok("用户注册成功！用户名: " + registeredUser.getUsername());
        } catch (UsernameAlreadyExistsException | EmailAlreadyExistsException e) {
            // 返回 409 Conflict 状态码 (由 @ResponseStatus 定义)
            // 或者手动返回 ResponseEntity
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("注册失败: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        try {
            LoginResponseDto loginResponse = authService.authenticateUser(loginRequestDto);
            return ResponseEntity.ok(loginResponse); // 返回包含 Token 和用户信息的 DTO
        } catch (AuthenticationException e) {
            // Spring Security 认证失败会抛出 AuthenticationException
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("登录失败: 用户名或密码错误");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("登录过程中发生错误: " + e.getMessage());
        }
    }
}