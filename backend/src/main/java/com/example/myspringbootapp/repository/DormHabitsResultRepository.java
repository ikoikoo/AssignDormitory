package com.example.myspringbootapp.repository;

import com.example.myspringbootapp.entity.DormHabitsResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface DormHabitsResultRepository extends JpaRepository<DormHabitsResult, Long> {
    // 可以添加根据用户查找结果的方法
    Optional<DormHabitsResult> findByUser_Id(Long userId);
    List<DormHabitsResult> findByUser_IdIn(List<Long> userIds);
}
