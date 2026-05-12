package com.example.myspringbootapp.controller;

import com.example.myspringbootapp.entity.Announcement;
import com.example.myspringbootapp.service.AnnouncementService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/announcements") // 公告 API 的基础路径
@CrossOrigin(origins = "http://localhost:5173") // 允许前端访问
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @GetMapping
    public ResponseEntity<List<Announcement>> getAllAnnouncements() {
        List<Announcement> announcements = announcementService.getAllAnnouncements();
        // 如果列表为空，返回空列表而不是 204 No Content，前端处理更方便
        return ResponseEntity.ok(announcements.isEmpty() ? Collections.emptyList() : announcements);
    }

    // --- 只有管理员能执行的操作 ---
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // <-- 只有 ADMIN 角色能访问
    public ResponseEntity<Announcement> createAnnouncement(@Valid @RequestBody Announcement announcement) {
        // 最好使用 DTO 接收数据
        announcement.setId(null); // 确保创建时 ID 为空
        announcement.setPublishDate(null); // 由 @CreationTimestamp 处理
        Announcement created = announcementService.createAnnouncement(announcement);
        return ResponseEntity.status(HttpStatus.CREATED).body(created); // 返回 201 Created
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // <-- 只有 ADMIN 角色能访问
    public ResponseEntity<Announcement> updateAnnouncement(@PathVariable Long id, @Valid @RequestBody Announcement announcementDetails) {
        // 最好使用 DTO
        Announcement updated = announcementService.updateAnnouncement(id, announcementDetails);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // <-- 只有 ADMIN 角色能访问
    public ResponseEntity<Void> deleteAnnouncement(@PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
        return ResponseEntity.noContent().build(); // 返回 204 No Content
    }
}
