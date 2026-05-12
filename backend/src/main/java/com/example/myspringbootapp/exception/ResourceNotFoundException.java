package com.example.myspringbootapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// @ResponseStatus 注解会让 Spring Boot 在此异常未被捕获时自动返回 404 Not Found
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException { // 通常继承 RuntimeException

    private static final long serialVersionUID = 1L; // 添加序列化版本 UID (好习惯)

    public ResourceNotFoundException(String message) {
        super(message); // 调用父类的构造函数
    }

    // 你可以根据需要添加其他构造函数或字段，例如保存资源类型和 ID
    // public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
    //    super(String.format("%s not found with %s : '%s'", resourceName, fieldName, fieldValue));
    // }
}
