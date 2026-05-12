package com.example.myspringbootapp.repository;


import com.example.myspringbootapp.entity.MbtiResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface MbtiResultRepository extends JpaRepository<MbtiResult, Long> {
    // 可以添加根据用户查找结果的方法
    Optional<MbtiResult> findByUser_Id(Long userId);
    List<MbtiResult> findByUser_IdIn(List<Long> userIds);
}
