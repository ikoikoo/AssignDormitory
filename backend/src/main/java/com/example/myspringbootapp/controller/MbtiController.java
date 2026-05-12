package com.example.myspringbootapp.controller;

import com.example.myspringbootapp.dto.MbtiSubmissionDto;
import com.example.myspringbootapp.entity.MbtiResult;
import com.example.myspringbootapp.service.MbtiService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid; // 启用 DTO 验证
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException; // 用于简单错误处理

@RestController
@RequestMapping("/api/users/{userId}/mbti") // 将 userId 作为路径参数
@CrossOrigin(origins = "http://localhost:5173") // 允许前端访问
public class MbtiController {

    @Autowired
    private MbtiService mbtiService;

    @PostMapping
    public ResponseEntity<MbtiResult> submitMbti(
            @PathVariable Long userId,
            @Valid @RequestBody MbtiSubmissionDto submissionDto) { // @Valid 启用验证
        try {
            MbtiResult savedResult = mbtiService.submitMbti(userId, submissionDto.getAnswers());
            return ResponseEntity.ok(savedResult); // 返回保存后的结果（包含ID和时间戳）
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            // 通用错误处理
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "提交MBTI问卷时发生错误", e);
        }
    }

    // (可选) 添加一个 GET 端点用于获取用户已有的 MBTI 结果
    @GetMapping
    public ResponseEntity<MbtiResult> getMbtiResult(@PathVariable Long userId) {
        // 实现查找逻辑...
        // MbtiResult result = mbtiService.getResultByUserId(userId);
        // return ResponseEntity.ok(result);
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build(); // 示例：暂未实现
    }
}