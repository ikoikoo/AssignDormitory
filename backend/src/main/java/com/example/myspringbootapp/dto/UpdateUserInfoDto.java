package com.example.myspringbootapp.dto;

import jakarta.validation.constraints.Email; // 邮箱格式验证
import jakarta.validation.constraints.Size;  // 字符串长度验证
import lombok.Data;

@Data
public class UpdateUserInfoDto {

    // 允许用户更新的字段
    @Email(message = "邮箱格式不正确")
    @Size(max = 255, message = "邮箱长度不能超过255个字符")
    private String email;

    @Size(max = 100, message = "姓名长度不能超过100个字符")
    private String name;

    // 可以添加其他允许更新的字段，例如联系电话等
    // 注意：通常不应允许用户通过此 DTO 更新 username 或 studentId
}
