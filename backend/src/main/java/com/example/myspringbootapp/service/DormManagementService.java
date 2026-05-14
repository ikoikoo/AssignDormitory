package com.example.myspringbootapp.service;

import com.example.myspringbootapp.dto.DormMemberInfoDto;
import com.example.myspringbootapp.entity.DormHabitsResult;
import com.example.myspringbootapp.entity.MbtiResult;
import com.example.myspringbootapp.entity.User;
import com.example.myspringbootapp.entity.DormAssignment;
import com.example.myspringbootapp.repository.DormAssignmentRepository;
import com.example.myspringbootapp.repository.DormHabitsResultRepository;
import com.example.myspringbootapp.repository.MbtiResultRepository;
import com.example.myspringbootapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;

import java.util.Map;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DormManagementService {

    @Autowired private UserRepository userRepository;
    @Autowired private MbtiResultRepository mbtiResultRepository;
    @Autowired private DormHabitsResultRepository dormHabitsResultRepository;
    @Autowired private DormAssignmentRepository dormAssignmentRepository;

    @Value("${app.python.script.path}")
    private String pythonScriptPath;

    @Value("${app.python.interpreter.path}")
    private String pythonInterpreterPath;

    @Value("${app.data.temp.dir}")
    private String tempDir;

    @PostConstruct
    public void checkInjectedPaths() {
        System.out.println("--- Injected Python Paths ---");
        System.out.println("Interpreter Path: [" + pythonInterpreterPath + "]");
        System.out.println("Script Path: [" + pythonScriptPath + "]");
        System.out.println("Temp Dir: [" + tempDir + "]");
        System.out.println("-----------------------------");
    }

    @Transactional(readOnly = true) // 这是一个只读操作
    public List<DormMemberInfoDto> getAllStudentSummaries() {
        // 1. 获取所有用户
        List<User> allUsers = userRepository.findAll();
        // 2. (优化) 批量获取所有 MBTI 和宿舍习惯结果，放入 Map 以便快速查找
        // 避免 N+1 查询问题
        List<Long> userIds = allUsers.stream().map(User::getId).collect(Collectors.toList());
        Map<Long, MbtiResult> mbtiResultsMap = mbtiResultRepository.findByUser_IdIn(userIds) // 使用 findByUser_IdIn 获取 List<MbtiResult>
                .stream()
                .filter(r -> r.getUser() != null) // 过滤脏数据
                // Key 是 user ID, Value 是整个 MbtiResult 对象
                .collect(Collectors.toMap(mbti -> mbti.getUser().getId(), mbti -> mbti));        Map<Long, DormHabitsResult> habitsResultsMap = dormHabitsResultRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(habits -> habits.getUser().getId(), habits -> habits));

        Map<Long, DormAssignment> assignmentMap = dormAssignmentRepository.findByUser_IdIn(userIds) // 假设 Repo 有 findByUser_IdIn
                .stream()
                .filter(a -> a.getUser() != null)
                .collect(Collectors.toMap(a -> a.getUser().getId(), a -> a));
        // 3. 遍历用户，组合信息到 DTO
        return allUsers.stream()
                .map(user -> {
                    // 从 Map 中查找对应的结果，找不到则给默认值或 null
                    MbtiResult mbti = mbtiResultsMap.get(user.getId());
                    DormHabitsResult habits = habitsResultsMap.get(user.getId());
                    DormAssignment assignment = assignmentMap.get(user.getId());
                    return new DormMemberInfoDto(
                            user.getId(),
                            user.getUsername(),
                            user.getName(),
                            user.getStudentId(),
                            user.getGender(),
                            (mbti != null) ? mbti.getMbtiType() : null, // MBTI 类型
                            (habits != null) ? habits.getRoutineScore() : null, // 作息分
                            (habits != null) ? habits.getHygieneScore() : null, // 卫生分
                            (habits != null) ? habits.getSocialScore() : null,  // 社交分
                            (habits != null) ? habits.getPreferenceFactors() : null, // 偏好
                            (habits != null) ? habits.getPilotParticipation() : null, // 试点
                            (assignment != null) ? assignment.getDormId() : null
                    );
                })
                .collect(Collectors.toList());
    }
    @Transactional
    public List<DormAssignment> runDormAssignmentViaPython() throws IOException, InterruptedException {
        // 1. 准备数据
        List<User> allUsers = userRepository.findAll();
        List<Long> userIds = allUsers.stream().map(User::getId).collect(Collectors.toList());
        Map<Long, MbtiResult> mbtiResultsMap = mbtiResultRepository.findByUser_IdIn(userIds) // <--- 获取数据
                .stream()
                .filter(r -> r.getUser() != null)
                .collect(Collectors.toMap(mbti -> mbti.getUser().getId(), mbti -> mbti));        Map<Long, DormHabitsResult> habitsMap = dormHabitsResultRepository.findAllById(userIds).stream()
                .filter(r -> r.getUser() != null)
                .collect(Collectors.toMap(habits -> habits.getUser().getId(), habits -> habits));
        Map<Long, DormHabitsResult> habitsResultsMap = dormHabitsResultRepository.findByUser_IdIn(userIds) // <--- 获取数据
                .stream()
                .filter(r -> r.getUser() != null)
                .collect(Collectors.toMap(habits -> habits.getUser().getId(), habits -> habits));
        // 筛选信息完整的用户
        List<User> eligibleUsers = allUsers.stream()
                .filter(user -> mbtiResultsMap.containsKey(user.getId()) && habitsResultsMap.containsKey(user.getId())) // <-- 使用 Map 检查
                .collect(Collectors.toList());

        if (eligibleUsers.isEmpty()) {
            System.out.println("Java: 没有足够信息的用户可供分配。");
            return Collections.emptyList();
        }
        System.out.println("Java: 找到 " + eligibleUsers.size() + " 位信息完整的用户。");


        // 2. 创建临时目录 (如果不存在)
        Path tempPath = Paths.get(tempDir);
        if (!Files.exists(tempPath)) {
            Files.createDirectories(tempPath);
        }

        // 3. 将数据写入临时输入文件 (CSV)
        String inputFileName = "dorm_input_" + System.currentTimeMillis() + ".csv";
        Path inputFile = tempPath.resolve(inputFileName);
        System.out.println("Java: 准备写入数据到: " + inputFile.toString());
        try (BufferedWriter writer = Files.newBufferedWriter(inputFile, StandardCharsets.UTF_8)) {
            // 写入 CSV Header
            writer.write("user_id,username,gender,mbti_type,routine_score,hygiene_score,social_score\n"); // 根据 Python 脚本需要的列调整
            // 写入数据行
            for (User user : eligibleUsers) {
                MbtiResult mbti = mbtiResultsMap.get(user.getId());
                DormHabitsResult habits = habitsResultsMap.get(user.getId());
                // 处理 null 值，写入空字符串或特定标记，确保 CSV 格式正确

                // --- 调试日志 ---
//                String genderValue = user.getGender();
//                String mbtiValue = (mbti != null ? mbti.getMbtiType() : "");
//                int routineScoreValue = (habits != null ? habits.getRoutineScore() : 0);
//                int hygieneScoreValue = (habits != null ? habits.getHygieneScore() : 0);
//                int socialScoreValue = (habits != null ? habits.getSocialScore() : 0);
//
//                System.out.printf("Debug - User ID: %d, Gender: [%s], MBTI: [%s], Routine: %d, Hygiene: %d, Social: %d%n",
//                        user.getId(), genderValue, mbtiValue, routineScoreValue, hygieneScoreValue, socialScoreValue);

                writer.write(String.format("%d,%s,%s,%s,%d,%d,%d\n",
                        user.getId(),
                        escapeCsv(user.getUsername()), // 处理可能包含逗号的字段
                        escapeCsv(user.getGender()),
                        escapeCsv(mbti != null ? mbti.getMbtiType() : ""),
                        habits != null ? habits.getRoutineScore() : 0, // 或其他默认值
                        habits != null ? habits.getHygieneScore() : 0,
                        habits != null ? habits.getSocialScore() : 0
                ));
            }
        }
        System.out.println("Java: 数据成功写入输入文件。");


        // 4. 准备输出文件名
        String outputFileName = "dorm_output_" + System.currentTimeMillis() + ".csv";
        Path outputFile = tempPath.resolve(outputFileName);

        // 5. 执行 Python 脚本
        System.out.println("Java: 开始执行 Python 脚本: " + pythonScriptPath);
        ProcessBuilder processBuilder = new ProcessBuilder(
                pythonInterpreterPath, // Python 解释器路径
                pythonScriptPath,      // 你的脚本路径
                "--input", inputFile.toString(),  // 输入文件参数
                "--output", outputFile.toString() // 输出文件参数
        );
        // 重定向脚本的错误流到 Java 的错误流，方便调试
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        // 读取 Python 脚本的标准输出
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("Python Output: " + line); // 打印 Python 脚本的输出
            }
        }

        // 等待 Python 脚本执行完成，并获取退出码
        int exitCode = process.waitFor();
        System.out.println("Java: Python 脚本执行完成，退出码: " + exitCode);

        // 6. 处理执行结果
        if (exitCode != 0) {
            // 文件清理 (即使失败也尝试清理)
            Files.deleteIfExists(inputFile);
            Files.deleteIfExists(outputFile); // 可能还未生成
            throw new RuntimeException("Python 脚本执行失败，退出码: " + exitCode);
        }

        // 7. 读取 Python 输出的结果文件
        System.out.println("Java: 读取 Python 输出文件: " + outputFile.toString());
        List<DormAssignment> newAssignments = new ArrayList<>();
        if (Files.exists(outputFile)) {
            try (BufferedReader reader = Files.newBufferedReader(outputFile)) {
                String header = reader.readLine(); // 读取并跳过 header
                if (header == null || !header.trim().equals("user_id,dorm_id")) {
                    Files.deleteIfExists(inputFile); Files.deleteIfExists(outputFile); // 清理
                    throw new RuntimeException("Python 输出文件格式不正确，缺少或错误的 Header: " + header);
                }
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 2) {
                        try {
                            Long userId = Long.parseLong(parts[0].trim());
                            Integer dormId = Integer.parseInt(parts[1].trim());
                            // 查找对应的 User 对象 (可以优化为 Map 查找)
                            User user = eligibleUsers.stream().filter(u -> u.getId().equals(userId)).findFirst()
                                    .orElse(null); // 理论上应该能找到
                            if (user != null) {
                                DormAssignment assignment = new DormAssignment();
                                assignment.setUser(user);
                                assignment.setDormId(dormId);
                                newAssignments.add(assignment);
                            } else {
                                System.err.println("Java Warning: 在结果文件中找到未知的 user_id: " + userId);
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("Java Error: 解析结果文件行时出错: '" + line + "' - " + e.getMessage());
                        }
                    }
                }
            }
        } else {
            Files.deleteIfExists(inputFile); // 清理
            throw new RuntimeException("Python 脚本执行成功，但未找到输出文件: " + outputFile.toString());
        }


        // 8. 清除旧的分配结果
        dormAssignmentRepository.deleteAllAssignments();
        System.out.println("Java: 已清除旧的宿舍分配结果。");

        // 9. 保存新的分配结果
        List<DormAssignment> savedAssignments = dormAssignmentRepository.saveAll(newAssignments);
        System.out.println("Java: 成功保存 " + savedAssignments.size() + " 条新的宿舍分配结果。");

        // 10. 清理临时文件
        Files.deleteIfExists(inputFile);
        Files.deleteIfExists(outputFile);
        System.out.println("Java: 已清理临时文件。");

        return savedAssignments;
    }

    // 简单的 CSV 转义，防止字段中的逗号或引号破坏格式
    private String escapeCsv(String value) {
        if (value == null) return "";
        value = value.replace("\"", "\"\""); // Escape double quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = "\"" + value + "\""; // Enclose in double quotes
        }
        return value;
    }

}
