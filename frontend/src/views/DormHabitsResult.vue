<template>
    <div class="page-container result-page">
        <header class="page-header">
            <h1>宿舍习惯评估结果</h1>
            <router-link to="/dashboard" class="back-link">返回个人主页</router-link>
        </header>
        <div class="content-area result-content">
            <div v-if="scoresLoaded" class="score-display">
                <p class="intro">根据您的问卷回答，您的习惯得分如下：</p>
                <ul class="score-list">
                    <li>
                        <strong>作息评分:</strong>
                        <span class="score-value">{{ scores.routine }}</span>
                        <span class="score-interpretation">{{ getInterpretation('routine', scores.routine) }}</span>
                    </li>
                    <li>
                        <strong>卫生评分:</strong>
                        <span class="score-value">{{ scores.hygiene }}</span>
                        <span class="score-interpretation">{{ getInterpretation('hygiene', scores.hygiene) }}</span>
                    </li>
                    <li>
                        <strong>社交评分:</strong>
                        <span class="score-value">{{ scores.social }}</span>
                        <span class="score-interpretation">{{ getInterpretation('social', scores.social) }}</span>
                    </li>
                </ul>
                <p class="reminder">
                    此评分旨在帮助您了解自己的生活习惯倾向，以便更好地适应集体生活。分数高低并无绝对好坏之分。
                </p>
                <router-link :to="{ name: 'DormHabitsForm' }" class="retake-link">重新填写问卷</router-link>
            </div>
            <div v-else class="loading-error">
                <p>无法加载评分结果。请确保您已完整填写问卷并提交。</p>
                <router-link :to="{ name: 'DormHabitsForm' }" class="back-link">返回问卷页面</router-link>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();
const scores = reactive({ routine: 0, hygiene: 0, social: 0 });
const scoresLoaded = ref(false);

// 在需要的地方获取 userId
const getUserId = () => {
    const userId = localStorage.getItem('user-id');
    if (!userId) {
        console.error("无法获取用户ID，请先登录！");
        // 可能需要重定向到登录页
        return null;
    }
    return parseInt(userId, 10); // 确保是数字
};

// 简单的分数解释（可扩展）
const interpretations = {
    routine: {
        high: "(倾向规律作息)", // 假设 4+ 分为高
        medium: "(作息较规律)", // 假设 2-3 分为中
        low: "(作息可能较随意)" // 假设 0-1 分为低
    },
    hygiene: {
        high: "(注重个人与公共卫生)", // 假设 3+ 分为高
        medium: "(卫生习惯尚可)", // 假设 1-2 分为中
        low: "(卫生习惯有待加强)" // 假设 0 分为低
    },
    social: {
        high: "(倾向活跃的宿舍社交)", // 假设 3+ 分为高
        medium: "(社交态度适中)", // 假设 1-2 分为中
        low: "(倾向安静或独立的宿舍生活)" // 假设 0 分为低
    }
};

// 获取解释的函数
const getInterpretation = (category, score) => {
    if (!interpretations[category]) return '';

    // 定义简单的分数阈值
    const thresholds = {
        routine: { high: 4, medium: 2 },
        hygiene: { high: 3, medium: 1 },
        social: { high: 3, medium: 1 }
    };

    if (score >= thresholds[category].high) {
        return interpretations[category].high;
    } else if (score >= thresholds[category].medium) {
        return interpretations[category].medium;
    } else {
        return interpretations[category].low;
    }
};


onMounted(() => {
    const { routineScore, hygieneScore, socialScore } = route.query;

    // 验证并解析分数
    const routine = parseInt(routineScore, 10);
    const hygiene = parseInt(hygieneScore, 10);
    const social = parseInt(socialScore, 10);

    if (!isNaN(routine) && !isNaN(hygiene) && !isNaN(social)) {
        scores.routine = routine;
        scores.hygiene = hygiene;
        scores.social = social;
        scoresLoaded.value = true;
    } else {
        console.error("无效或丢失的习惯评分参数:", route.query);
        scoresLoaded.value = false;
    }
});

</script>

<style scoped>
.page-container {
    padding: 20px;
    max-width: 700px;
    margin: 20px auto;
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
    border-radius: 8px;
    border: 1px solid #eee;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.intro {
    font-size: 1.1em;
    color: #333;
    margin-bottom: 25px;
    text-align: center;
}

.score-list {
    list-style: none;
    padding: 0;
    margin: 0;
    border: 1px solid #eee;
    border-radius: 5px;
    overflow: hidden;
    /* 配合圆角 */
}

.score-list li {
    display: flex;
    /* 使用 flex 布局对齐 */
    align-items: center;
    padding: 15px 20px;
    border-bottom: 1px solid #eee;
    background-color: #f8f9fa;
    font-size: 1.1em;
}

.score-list li:last-child {
    border-bottom: none;
}

.score-list li strong {
    color: #555;
    min-width: 100px;
    /* 确保标签宽度一致 */
    margin-right: 15px;
}

.score-value {
    font-size: 1.5em;
    /* 突出分数 */
    font-weight: bold;
    color: #28a745;
    /* 绿色表示分数 */
    margin-right: 15px;
    /* 和解释文字的间距 */
    min-width: 30px;
    /* 确保分数有一定宽度 */
    text-align: right;
}

.score-interpretation {
    font-size: 0.9em;
    color: #6c757d;
    font-style: italic;
}

.reminder {
    margin-top: 30px;
    font-size: 0.95em;
    color: #555;
    line-height: 1.6;
    text-align: center;
    padding: 15px;
    background-color: #eef2f7;
    border-radius: 4px;
}

.loading-error {
    color: #dc3545;
    text-align: center;
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
    margin-left: auto;
    margin-right: auto;
    display: block;
    width: fit-content;
}

.retake-link:hover {
    background-color: #e0a800;
}
</style>