package com.example.myspringbootapp.controller;

import com.example.myspringbootapp.dto.DormMemberInfoDto;
import com.example.myspringbootapp.entity.DormAssignment;
import com.example.myspringbootapp.entity.DormHabitsResult;
import com.example.myspringbootapp.service.DormHabitsService;
import com.example.myspringbootapp.service.DormManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // 导入 PreAuthorize
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/admin/dorms") // 管理员接口的基础路径
@CrossOrigin(origins = "http://localhost:5173") // 允许前端访问
public class DormManagementController {

    @Autowired
    private DormManagementService dormManagementService;

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DormMemberInfoDto>> getDormSummaries() {
        List<DormMemberInfoDto> summaries = dormManagementService.getAllStudentSummaries();
        if (summaries.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(summaries);
    }
    // --- ---

    // --- 添加触发分配的接口 ---
    @PostMapping("/run-assignment") // 使用新的路径区分
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> runAssignments() {
        try {
            List<DormAssignment> results = dormManagementService.runDormAssignmentViaPython();
            if (results.isEmpty()) {
                return ResponseEntity.ok("没有足够的用户数据来执行分配，或者分配未产生结果。");
            }
            return ResponseEntity.ok("宿舍分配成功执行！共分配 " + results.size() + " 名学生。");
        } catch (Exception e) {
            System.err.println("执行宿舍分配时发生错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("执行宿舍分配时发生内部错误: " + e.getMessage());
        }
    }
}