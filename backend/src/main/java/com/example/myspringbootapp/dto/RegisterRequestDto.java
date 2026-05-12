package com.example.myspringbootapp.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequestDto {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在 3 到 20 之间")
    private String username;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 50, message = "邮箱长度不能超过 50")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 40, message = "密码长度必须在 6 到 40 之间")
    private String password;

    @Size(max = 10, message = "性别长度不能超过 10") // 可选：添加长度验证
    private String gender;

    @NotBlank(message = "姓名不能为空")
    private String name;

    @NotBlank(message = "学号不能为空")
    private String studentId;
}
