<template>
    <div class="page-container result-page">
        <header class="page-header">
            <h1>MBTI 测试结果</h1>
            <router-link to="/dashboard" class="back-link">返回个人主页</router-link>
        </header>
        <div class="content-area result-content">
            <div v-if="mbtiResult" class="result-display">
                <p class="result-label">您的 MBTI 类型是：</p>
                <p class="result-type">{{ mbtiResult }}</p>
                <!-- 你可以在这里根据不同的 MBTI 类型显示更详细的描述 -->
                <div class="result-description">
                    <p>这是一个基于您问卷选择的初步评估结果。</p>
                    <p>了解您的 MBTI 类型有助于更好地认识自己、理解他人以及改善人际关系和职业发展。</p>
                    <!-- 示例：可以添加一个简单的类型描述 -->
                    <div v-if="descriptions[mbtiResult]">
                        <h4>{{ mbtiResult }} 类型简述:</h4>
                        <p>{{ descriptions[mbtiResult] }}</p>
                    </div>
                    <p v-else>
                        (暂无该类型的详细描述)
                    </p>
                    <p class="reminder">请注意：MBTI 仅为一种性格评估工具，结果仅供参考，人的性格是复杂且动态发展的。</p>
                </div>
                <router-link :to="{ name: 'MbtiForm' }" class="retake-link">重新测试</router-link>
            </div>
            <div v-else class="loading-error">
                <p>无法加载测试结果。可能是结果参数丢失或无效。</p>
                <router-link :to="{ name: 'MbtiForm' }" class="back-link">返回问卷页面</router-link>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();
const mbtiResult = ref('');

// 简单的类型描述（可以扩展或从外部数据源加载）
const descriptions = ref({
    'ISTJ': '认真、负责、注重事实。喜欢让生活和工作井井有条。',
    'ISFJ': '安静、友好、负责任。致力于履行自己的义务。',
    'INFJ': '寻求意义和联系。关心他人，有洞察力。',
    'INTJ': '有创见，思想独立。有强大的动力去实现目标。',
    'ISTP': '灵活、容忍、务实。对事物如何运作感兴趣。',
    'ISFP': '安静、友好、敏感。享受当下，不喜欢冲突。',
    'INFP': '理想主义、忠诚。希望外部生活与内在价值观一致。',
    'INTP': '寻求逻辑解释。更感兴趣的是想法而非社交互动。',
    'ESTP': '灵活、容忍，注重实效。喜欢通过行动解决问题。',
    'ESFP': '外向、友好、随和。热爱生活、人和物质享受。',
    'ENFP': '热情、富有想象力。乐于赞赏他人，需要肯定。',
    'ENTP': '反应快、足智多谋。善于分析，不喜欢例行公事。',
    'ESTJ': '务实、果断，天生的领导者。喜欢组织项目和人员。',
    'ESFJ': '热心、乐于助人。希望被欣赏，喜欢为他人服务。',
    'ENFJ': '热情、有同情心。能察觉他人的情感、需求和动机。',
    'ENTJ': '坦率、果断，天生的领导者。善于发现低效率并改进。'
});


onMounted(() => {
    // 从路由参数中获取结果
    const resultFromRoute = route.params.result;
    if (resultFromRoute && /^[IE][SN][TF][JP]$/.test(resultFromRoute)) { // 验证结果格式
        mbtiResult.value = resultFromRoute;
    } else {
        console.error("无效或丢失的 MBTI 结果参数:", resultFromRoute);
        // 可以在这里处理错误，例如重定向回问卷页面
    }
});

</script>

<style scoped>
.page-container {
    padding: 20px;
    max-width: 700px;
    margin: 20px auto;
    /* 上下边距 */
}

.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 10px;
    border-bottom: 1px solid #eee;
}

.page-header h1 {
    margin: 0;
    color: #333;
    font-size: 1.6em;
}

.back-link {
    padding: 8px 12px;
    background-color: #6c757d;
    color: white;
    text-decoration: none;
    border-radius: 4px;
    transition: background-color 0.3s;
}

.back-link:hover {
    background-color: #5a6268;
}

.content-area {
    background-color: #fff;
    padding: 30px;
    /* 结果页内边距可以大一些 */
    border-radius: 8px;
    border: 1px solid #eee;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    text-align: center;
    /* 内容居中 */
}

.result-display {
    /* 样式留给内部元素 */
}

.result-label {
    font-size: 1.2em;
    color: #666;
    margin-bottom: 5px;
}

.result-type {
    font-size: 3em;
    /* 突出显示结果 */
    font-weight: bold;
    color: #007bff;
    /* 主题色 */
    margin-bottom: 25px;
    letter-spacing: 2px;
    /* 字母间距 */
}

.result-description {
    text-align: left;
    /* 描述文本左对齐 */
    margin-top: 20px;
    padding: 20px;
    background-color: #f8f9fa;
    border-radius: 5px;
    border: 1px solid #eee;
    color: #444;
    line-height: 1.7;
}

.result-description h4 {
    color: #0056b3;
    margin-bottom: 10px;
}

.reminder {
    margin-top: 15px;
    font-size: 0.9em;
    font-style: italic;
    color: #777;
}

.loading-error {
    color: #dc3545;
}

.loading-error p {
    margin-bottom: 15px;
}

.retake-link {
    display: inline-block;
    margin-top: 30px;
    padding: 10px 20px;
    background-color: #ffc107;
    color: #333;
    border-radius: 5px;
    text-decoration: none;
    font-weight: bold;
    transition: background-color 0.3s;
}

.retake-link:hover {
    background-color: #e0a800;
}
</style>