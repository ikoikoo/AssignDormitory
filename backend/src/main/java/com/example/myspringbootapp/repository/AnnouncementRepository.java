package com.example.myspringbootapp.repository;

import com.example.myspringbootapp.entity.Announcement;
import org.springframework.data.domain.Sort; // 导入 Sort
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List; // 导入 List

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    // JpaRepository 已经提供了 findAll() 方法
    // 如果想确保总是按发布日期降序排序，可以覆盖或添加新方法：
    List<Announcement> findAllByOrderByPublishDateDesc();

    // 或者在 Service 层调用 findAll(Sort sort)
}
