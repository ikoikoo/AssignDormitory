package com.example.myspringbootapp.init;

import com.example.myspringbootapp.entity.DormHabitsResult;
import com.example.myspringbootapp.entity.MbtiResult;
import com.example.myspringbootapp.entity.User;
import com.example.myspringbootapp.repository.DormHabitsResultRepository;
import com.example.myspringbootapp.repository.MbtiResultRepository;
import com.example.myspringbootapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder; // 如果需要加密密码

import java.util.*;
import java.util.concurrent.ThreadLocalRandom; // 更推荐用于多线程，单线程 Random 也可
import java.util.stream.Collectors;
@Component // 让 Spring 管理这个 Bean
public class BatchDataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MbtiResultRepository mbtiResultRepository;
    @Autowired
    private DormHabitsResultRepository dormHabitsResultRepository;
    @Autowired // 如果配置了 Spring Security，注入 PasswordEncoder
    private PasswordEncoder passwordEncoder;

    private static final int NUMBER_OF_USERS_TO_CREATE = 50; // 定义要创建的用户数量
    private static final List<String> GENDERS = Arrays.asList("男", "女");
    private static final String[] MBTI_IE = {"I", "E"};
    private static final String[] MBTI_SN = {"S", "N"};
    private static final String[] MBTI_TF = {"T", "F"};
    private static final String[] MBTI_JP = {"J", "P"};
    private static final String[] PREF_FACTORS = {"routine", "hygiene", "personality", "interest"};
    private static final String[] PILOT_OPTIONS = {"yes", "no"};

    @Override
    @Transactional // 将整个初始化过程放在一个事务中
    public void run(String... args) throws Exception {
        long existingUsers = userRepository.count();
        if (existingUsers == 0) { // 只在数据库为空时执行初始化
            System.out.println("数据库为空，开始批量创建测试用户及相关数据...");
            createBatchUsersAndData(NUMBER_OF_USERS_TO_CREATE);
            System.out.println("批量数据创建完成！");
            // --- 创建管理员用户 ---
            System.out.println("正在创建管理员用户...");
            User adminUser = new User();
            adminUser.setUsername("admin"); // 管理员用户名
            adminUser.setEmail("admin@example.com");
            adminUser.setPassword(passwordEncoder.encode("adminpass")); // 设置密码并加密 (请使用强密码!)
            adminUser.setName("系统管理员");
            adminUser.setStudentId("ADMIN001");
            adminUser.setGender("其他");
            adminUser.setRole("ROLE_ADMIN"); // <-- 关键：设置角色为 ADMIN
            userRepository.save(adminUser);
            System.out.println("管理员用户创建成功，ID: " + adminUser.getId());
        } else if (!userRepository.existsByUsername("admin")) {
            // 可选：如果用户已存在但没有 admin，也创建一个
            System.out.println("未找到管理员用户，正在创建...");
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@example.com");
            adminUser.setPassword(passwordEncoder.encode("adminpass"));
            adminUser.setName("系统管理员");
            adminUser.setStudentId("ADMIN001");
            adminUser.setGender("其他");
            adminUser.setRole("ROLE_ADMIN");
            userRepository.save(adminUser);
            System.out.println("管理员用户创建成功，ID: " + adminUser.getId());
        } else {
            System.out.println("数据库中已存在用户 (" + existingUsers + " 个)，跳过批量数据初始化。");
//            // 输出一些现有用户ID供前端测试
//            userRepository.findAll().stream().limit(5).forEach(u ->
//                    System.out.println("现有用户示例: ID=" + u.getId() + ", Username=" + u.getUsername())
//            );
        }
    }

    private void createBatchUsersAndData(int count) {
        Random random = ThreadLocalRandom.current(); // 获取随机数生成器
        List<User> savedUsers = new ArrayList<>();

        // --- 1. 创建用户 ---
        for (int i = 1; i <= count; i++) {
            User user = new User();
            String username = "testuser" + String.format("%03d", i); // e.g., testuser001
            user.setUsername(username);
            user.setEmail(username + "@test.com");
            user.setName("测试员" + String.format("%03d", i));
            user.setStudentId("S" + String.format("%04d", i)); // e.g., S0001
            user.setGender(GENDERS.get(random.nextInt(GENDERS.size())));
            // user.setPassword(passwordEncoder.encode("password123")); // 设置默认密码并加密

            // 检查唯一性（对于大批量数据，先生成后检查可能更高效，或依赖数据库约束）
            // 这里简化处理，假设生成的不会立即冲突
            savedUsers.add(userRepository.save(user)); // 保存用户并添加到列表，以便后续使用 ID
        }
        System.out.println("成功创建 " + savedUsers.size() + " 个用户。");

        // --- 2. 为每个用户创建 MBTI 结果 ---
        List<MbtiResult> mbtiResults = new ArrayList<>();
        for (User user : savedUsers) {
            MbtiResult mbtiResult = new MbtiResult();
            mbtiResult.setUser(user); // 关联用户
            String ie = MBTI_IE[random.nextInt(MBTI_IE.length)];
            String sn = MBTI_SN[random.nextInt(MBTI_SN.length)];
            String tf = MBTI_TF[random.nextInt(MBTI_TF.length)];
            String jp = MBTI_JP[random.nextInt(MBTI_JP.length)];
            mbtiResult.setMbtiType(ie + sn + tf + jp);
            // submissionTimestamp 由 @CreationTimestamp 自动生成
            mbtiResults.add(mbtiResult);
        }
        mbtiResultRepository.saveAll(mbtiResults); // 批量保存 MBTI 结果
        System.out.println("成功为 " + mbtiResults.size() + " 个用户创建了 MBTI 结果。");


        // --- 3. 为每个用户创建宿舍习惯结果 ---
        List<DormHabitsResult> dormHabitsResults = new ArrayList<>();
        for (User user : savedUsers) {
            DormHabitsResult habitsResult = new DormHabitsResult();
            habitsResult.setUser(user); // 关联用户

            // 生成随机分数 (保持不变)
            habitsResult.setRoutineScore(random.nextInt(7));
            habitsResult.setHygieneScore(random.nextInt(5));
            habitsResult.setSocialScore(random.nextInt(5));

            // 生成随机偏好因素 (保持不变)
            int numberOfFactors = random.nextInt(PREF_FACTORS.length) + 1;
            List<String> factors = new ArrayList<>(Arrays.asList(PREF_FACTORS));
            Collections.shuffle(factors);
            // --- 确保 Collectors 被导入后，这行就能正常工作了 ---
            String selectedFactors = factors.subList(0, numberOfFactors).stream().sorted().collect(Collectors.joining(","));
            habitsResult.setPreferenceFactors(selectedFactors);

            // 生成随机试点参与意愿 (保持不变)
            habitsResult.setPilotParticipation(PILOT_OPTIONS[random.nextInt(PILOT_OPTIONS.length)]);

            dormHabitsResults.add(habitsResult);
        }
        dormHabitsResultRepository.saveAll(dormHabitsResults);
        System.out.println("成功为 " + dormHabitsResults.size() + " 个用户创建了宿舍习惯结果。");
    }
}