package com.example.myspringbootapp.service;

import com.example.myspringbootapp.entity.Announcement;
import com.example.myspringbootapp.repository.AnnouncementRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class AnnouncementService {

    @Autowired
    private AnnouncementRepository announcementRepository;

    public List<Announcement> getAllAnnouncements() {
        // 获取所有公告，并按发布日期降序排序
        // 方法一：使用 Repository 中定义的方法
        // return announcementRepository.findAllByOrderByPublishDateDesc();

        // 方法二：使用 JpaRepository 的 findAll(Sort)
        return announcementRepository.findAll(Sort.by(Sort.Direction.DESC, "publishDate"));
    }

    @Transactional // 添加事务注解
    public Announcement createAnnouncement(Announcement announcement) {
        // publishDate 会自动设置
        return announcementRepository.save(announcement);
    }

    @Transactional // 添加事务注解
    public Announcement updateAnnouncement(Long id, Announcement announcementDetails) {
        Announcement existing = announcementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("未找到ID为 " + id + " 的公告"));
        existing.setTitle(announcementDetails.getTitle());
        existing.setContent(announcementDetails.getContent());
        return announcementRepository.save(existing);
    }

    @Transactional // 添加事务注解
    public void deleteAnnouncement(Long id) {
        if (!announcementRepository.existsById(id)) {
            throw new EntityNotFoundException("未找到ID为 " + id + " 的公告，无法删除");
        }
        announcementRepository.deleteById(id);
    }}