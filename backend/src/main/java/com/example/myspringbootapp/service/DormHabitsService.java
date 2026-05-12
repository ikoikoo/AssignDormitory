package com.example.myspringbootapp.service;

import com.example.myspringbootapp.dto.DormHabitsSubmissionDto;
import com.example.myspringbootapp.entity.DormHabitsResult;
import com.example.myspringbootapp.entity.User;
import com.example.myspringbootapp.repository.DormHabitsResultRepository;
import com.example.myspringbootapp.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DormHabitsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DormHabitsResultRepository dormHabitsResultRepository;
    public List<DormHabitsResult> getAllDormHabitsResults() {
        // 后续可以添加排序或分页
        return dormHabitsResultRepository.findAll();
    }
    // 评分规则定义 (与前端一致)
    private static final Map<String, Map<String, Integer>> SCORE_RULES = Map.of(
            "q_sleep_time", Map.of("before_22", 4, "22_to_2330", 3, "2330_to_1", 2, "after_1", 1),
            "q_nap", Map.of("daily", 2, "often", 1, "occasionally", 0, "never", 0),
            "q_tidy", Map.of("daily", 2, "weekly_2_3", 1, "monthly_1_2", 0, "rarely", 0),
            "q_cleaning", Map.of("proactive", 2, "on_schedule", 1, "occasionally", 0, "rarely", 0),
            "q_activities", Map.of("very_much", 2, "neutral", 1, "no", 0),
            "q_visitors", Map.of("daily", 2, "weekly_2_3", 1, "monthly_1_2", 0, "never", 0)
            // 其他不计分的问题不在此处定义
    );

    private static final Map<String, String> CATEGORY_MAP = Map.of(
            "q_sleep_time", "routine",
            "q_nap", "routine",
            "q_tidy", "hygiene",
            "q_cleaning", "hygiene",
            "q_activities", "social",
            "q_visitors", "social"
    );


    @Transactional
    public DormHabitsResult submitDormHabits(Long userId, DormHabitsSubmissionDto dto) {
        System.out.println("Service 层接收到的 userId: " + userId);
        // 1. 查找用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("未找到ID为 " + userId + " 的用户"));
        System.out.println("查找到的用户信息: ID=" + user.getId() + ", Username=" + user.getUsername());
        // 2. 计算分数
        Scores calculatedScores = calculateScores(dto);

        // 3. 创建或更新结果
        Optional<DormHabitsResult> existingResultOpt = dormHabitsResultRepository.findByUser_Id(userId);
        DormHabitsResult resultToSave;

        if (existingResultOpt.isPresent()) {
            resultToSave = existingResultOpt.get();
            resultToSave.setRoutineScore(calculatedScores.routineScore);
            resultToSave.setHygieneScore(calculatedScores.hygieneScore);
            resultToSave.setSocialScore(calculatedScores.socialScore);
            resultToSave.setPreferenceFactors(String.join(",", dto.getQ_preference_factors())); // 更新多选
            resultToSave.setPilotParticipation(dto.getQ_pilot()); // 更新单选
            resultToSave.setSubmissionTimestamp(LocalDateTime.now()); // 更新时间
        } else {
            resultToSave = new DormHabitsResult();
            resultToSave.setUser(user);
            resultToSave.setRoutineScore(calculatedScores.routineScore);
            resultToSave.setHygieneScore(calculatedScores.hygieneScore);
            resultToSave.setSocialScore(calculatedScores.socialScore);
            // 将 List<String> 转换为逗号分隔的 String 存储
            resultToSave.setPreferenceFactors(String.join(",", dto.getQ_preference_factors()));
            resultToSave.setPilotParticipation(dto.getQ_pilot());
            // @CreationTimestamp 会自动设置创建时间
        }
        // 4. 保存到数据库
        return dormHabitsResultRepository.save(resultToSave);
    }

    // 计算分数逻辑 (必须与前端一致)
    private Scores calculateScores(DormHabitsSubmissionDto dto) {
        Scores scores = new Scores();

        // 辅助方法，根据问题ID和选中值获取分数
        java.util.function.BiFunction<String, String, Integer> getScore = (questionId, selectedValue) ->
                SCORE_RULES.getOrDefault(questionId, Map.of()).getOrDefault(selectedValue, 0);

        // 辅助方法，根据问题ID获取类别
        java.util.function.Function<String, String> getCategory = CATEGORY_MAP::get;


        // 遍历 DTO 的所有字段进行计分（更健壮的方式）
        // 使用反射或者手动列出计分项
        Map<String, Object> dtoMap = convertDtoToMap(dto); // 需要一个转换方法

        for(Map.Entry<String, Object> entry : dtoMap.entrySet()){
            String questionId = entry.getKey();
            Object value = entry.getValue();

            if(value instanceof String && SCORE_RULES.containsKey(questionId)){
                int score = getScore.apply(questionId, (String) value);
                String category = getCategory.apply(questionId);
                if("routine".equals(category)) scores.routineScore += score;
                else if("hygiene".equals(category)) scores.hygieneScore += score;
                else if("social".equals(category)) scores.socialScore += score;
            }
            // Checkbox (q_preference_factors) 和其他不计分的不处理
        }


        return scores;
    }

    // 内部类用于返回多个分数
    private static class Scores {
        int routineScore = 0;
        int hygieneScore = 0;
        int socialScore = 0;
    }

    // 简单的 DTO 转 Map (可以使用 Jackson ObjectMapper 更健壮)
    private Map<String, Object> convertDtoToMap(DormHabitsSubmissionDto dto) {
        java.util.HashMap<String, Object> map = new java.util.HashMap<>();
        map.put("q_sleep_time", dto.getQ_sleep_time());
        map.put("q_wake_time", dto.getQ_wake_time());
        map.put("q_nap", dto.getQ_nap());
        map.put("q_tidy", dto.getQ_tidy());
        map.put("q_cleaning", dto.getQ_cleaning());
        map.put("q_activities", dto.getQ_activities());
        map.put("q_visitors", dto.getQ_visitors());
        map.put("q_sharing", dto.getQ_sharing());
        map.put("q_preference_factors", dto.getQ_preference_factors());
        map.put("q_pilot", dto.getQ_pilot());
        return map;
    }
}
