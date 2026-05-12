package com.example.myspringbootapp.controller;

import com.example.myspringbootapp.dto.DormHabitsSubmissionDto;
import com.example.myspringbootapp.entity.DormHabitsResult;
import com.example.myspringbootapp.service.DormHabitsService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Collections;
import com.example.myspringbootapp.entity.DormHabitsResult; // 导入实体类
import com.example.myspringbootapp.service.DormHabitsService; // 导入服务
import org.springframework.security.access.prepost.PreAuthorize; // 导入 PreAuthorize
import org.springframework.web.bind.annotation.GetMapping; // 导入 GetMapping

@RestController
@RequestMapping("/api/users/{userId}/dorm-habits")
@CrossOrigin(origins = "http://localhost:5173")
public class DormHabitsController {

    @Autowired
    private DormHabitsService dormHabitsService;

    @PostMapping
    public ResponseEntity<DormHabitsResult> submitDormHabits(
            @PathVariable Long userId,
            @Valid @RequestBody DormHabitsSubmissionDto submissionDto) {
        try {
            DormHabitsResult savedResult = dormHabitsService.submitDormHabits(userId, submissionDto);
            return ResponseEntity.ok(savedResult); // 返回保存后的结果
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "提交宿舍习惯问卷时发生错误", e);
        }
    }

    // (可选) 添加 GET 端点获取结果
    @GetMapping
    public ResponseEntity<DormHabitsResult> getDormHabitsResult(@PathVariable Long userId) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
