package com.example.myspringbootapp.service;

import com.example.myspringbootapp.entity.MbtiResult;
import com.example.myspringbootapp.entity.User;
import com.example.myspringbootapp.repository.MbtiResultRepository;
import com.example.myspringbootapp.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException; // 或者自定义异常
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 确保数据库操作原子性

import java.util.Map;
import java.util.Optional;

@Service
public class MbtiService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MbtiResultRepository mbtiResultRepository;

    @Transactional // 推荐在修改数据库的服务方法上添加事务管理
    public MbtiResult submitMbti(Long userId, Map<String, String> answers) {
        // 1. 查找用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("未找到ID为 " + userId + " 的用户"));

        // 2. 计算 MBTI 类型 (与前端逻辑一致)
        String mbtiType = calculateMbtiType(answers);

        // 3. 创建或更新结果
        // 检查用户是否已有结果，如果有则更新，没有则创建
        Optional<MbtiResult> existingResultOpt = mbtiResultRepository.findByUser_Id(userId);

        MbtiResult resultToSave;
        if (existingResultOpt.isPresent()) {
            // 更新现有记录
            resultToSave = existingResultOpt.get();
            resultToSave.setMbtiType(mbtiType);
            // submissionTimestamp 会自动更新（如果使用 @UpdateTimestamp）或者保持创建时间（如果用 @CreationTimestamp）
            // 这里我们假设每次提交都算新的记录时间，所以最好是新建或手动更新时间
            resultToSave.setSubmissionTimestamp(java.time.LocalDateTime.now()); // 手动更新提交时间
        } else {
            // 创建新记录
            resultToSave = new MbtiResult();
            resultToSave.setUser(user);
            resultToSave.setMbtiType(mbtiType);
        }
        // 4. 保存到数据库
        return mbtiResultRepository.save(resultToSave);
    }

    // MBTI 计算逻辑 (必须与前端一致！)
    private String calculateMbtiType(Map<String, String> answers) {
        if (answers == null || answers.isEmpty()) {
            throw new IllegalArgumentException("MBTI 答案不能为空");
        }

        Map<String, Integer> counts = Map.of(
                "I", 0, "E", 0, "S", 0, "N", 0, "T", 0, "F", 0, "J", 0, "P", 0
        );
        // 创建一个可变的 Map 用于计数
        java.util.HashMap<String, Integer> mutableCounts = new java.util.HashMap<>(counts);


        // 假设问题 ID q1-q5 对应 I/E, q6-q10 对应 S/N, q11-q15 对应 T/F, q16-q20 对应 J/P
        // **重要**: 这个映射关系必须严格根据你的 DTO 和前端逻辑来确定
        for (Map.Entry<String, String> entry : answers.entrySet()) {
            String answerValue = entry.getValue();
            if (mutableCounts.containsKey(answerValue)) {
                mutableCounts.put(answerValue, mutableCounts.get(answerValue) + 1);
            }
        }

        if (mutableCounts.values().stream().mapToInt(Integer::intValue).sum() < 20) {
            // 可以添加更严格的验证，确保正好有20个答案被计数
            System.out.println("警告：计算MBTI时答案数量不足20个或存在无效值。");
        }


        String ie = mutableCounts.get("I") > mutableCounts.get("E") ? "I" : "E";
        String sn = mutableCounts.get("S") > mutableCounts.get("N") ? "S" : "N";
        String tf = mutableCounts.get("T") > mutableCounts.get("F") ? "T" : "F";
        String jp = mutableCounts.get("J") > mutableCounts.get("P") ? "J" : "P";

        return ie + sn + tf + jp;
    }
}
