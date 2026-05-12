<template>
    <div class="page-container">
        <header class="page-header">
            <h1>宿舍管理</h1>
            <router-link to="/dashboard" class="back-link">返回个人主页</router-link>
        </header>

        <div class="content-area">
            <!-- ======== 执行分配的操作区域 ======== -->
            <div class="assignment-controls">
                <button @click="runAssignment" :disabled="isRunningAssignment" class="run-button">
                    <!-- 按钮图标和文字，根据 isRunningAssignment 变化 -->
                    <i v-if="isRunningAssignment" class="fas fa-spinner fa-spin"></i>
                    {{ isRunningAssignment ? '正在分配...' : '执行宿舍分配' }}
                </button>
                <!-- 显示操作结果的消息 -->
                <p v-if="assignmentMessage" :class="{ 'success': assignmentSuccess, 'error': !assignmentSuccess }"
                    class="assignment-message">
                    {{ assignmentMessage }}
                </p>
            </div>
            <hr> <!-- 分隔线 -->
            <h2>学生信息总览</h2>
            <!-- ... (你现有的显示学生信息总览的表格或列表) ... -->
            <div v-if="loading" class="loading-message">正在加载学生信息...</div>
            <div v-else-if="loadError" class="error-message">加载失败: {{ loadError }}</div>
            <div v-else-if="students.length === 0" class="no-data-message">暂无学生信息。</div>

            <table v-else class="students-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>用户名</th>
                        <th>姓名</th>
                        <th>学号</th>
                        <th>性别</th>
                        <th>MBTI</th>
                        <th>作息分</th>
                        <th>卫生分</th>
                        <th>社交分</th>
                        <th>偏好</th>
                        <th>试点</th>
                        <th>宿舍号</th>
                    </tr>
                </thead>
                <tbody>
                    <!-- 遍历后端返回的 DormMemberInfoDto 列表 -->
                    <tr v-for="student in students" :key="student.userId">
                        <td>{{ student.userId }}</td>
                        <td>{{ student.username }}</td>
                        <td>{{ student.name || '-' }}</td>
                        <td>{{ student.studentId || '-' }}</td>
                        <td>{{ student.gender || '-' }}</td>
                        <td>{{ student.mbtiType || '-' }}</td>
                        <td>{{ student.routineScore ?? '-' }}</td> <!-- 使用 ?? 处理 null/undefined -->
                        <td>{{ student.hygieneScore ?? '-' }}</td>
                        <td>{{ student.socialScore ?? '-' }}</td>
                        <td class="factors-cell">{{ student.preferenceFactors || '-' }}</td>
                        <td>{{ student.pilotParticipation || '-' }}</td>
                        <td>{{ student.dormId ?? '未分配' }}</td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import apiClient from '@/api/axios'; // 引入配置好的 Axios 实例

const students = ref([]);      // 存储学生信息列表
const loading = ref(true);     // 加载状态
const loadError = ref(null);   // 加载错误

// 获取学生摘要信息的函数
const fetchStudentSummaries = async () => {
    loading.value = true;
    loadError.value = null;
    console.log("尝试获取学生摘要信息 (/api/admin/dorms/summary)...");
    try {
        const response = await apiClient.get('/admin/dorms/summary'); // 获取学生列表的接口
        students.value = response.data || [];
    } catch (err) {
        loadError.value = err.response?.data?.message || err.message || '无法加载学生数据';
        students.value = [];
    } finally {
        loading.value = false;
    }
};
// --- ---

// --- 新增：用于控制分配按钮和反馈的状态 ---
const isRunningAssignment = ref(false); // 是否正在执行分配操作
const assignmentMessage = ref(null);   // 显示给用户的分配结果消息
const assignmentSuccess = ref(false);  // 标记消息是成功还是失败 (用于CSS样式)
// --- ---

// --- 新增：触发后端分配任务的方法 ---
const runAssignment = async () => {
    isRunningAssignment.value = true; // 开始执行，禁用按钮
    assignmentMessage.value = null;   // 清除旧消息

    // 添加一个确认框，防止误操作
    if (!window.confirm("确定要执行新的宿舍分配吗？这可能会覆盖之前的分配结果，且可能需要一些时间。")) {
        isRunningAssignment.value = false; // 用户取消，恢复按钮
        return;
    }

    console.log("前端：尝试触发宿舍分配 (POST /api/admin/dorms/run-assignment)...");
    try {
        // 调用后端的 POST 接口，确保 apiClient 会自动附加 Authorization Token
        const response = await apiClient.post('/admin/dorms/run-assignment'); // 不需要发送请求体

        console.log("前端：宿舍分配执行成功，后端响应:", response.data);
        assignmentMessage.value = response.data || "宿舍分配成功执行！"; // 显示后端返回的消息
        assignmentSuccess.value = true; // 标记为成功

        // （可选）分配成功后是否需要刷新学生列表？
        // 如果学生列表需要显示新的宿舍号，可以在这里调用 fetchStudentSummaries()
        // await fetchStudentSummaries();

    } catch (err) {
        console.error('前端：执行宿舍分配失败:', err);
        assignmentMessage.value = err.response?.data || err.message || '执行分配时发生错误'; // 显示错误信息
        assignmentSuccess.value = false; // 标记为失败
    } finally {
        isRunningAssignment.value = false; // 无论成功失败，恢复按钮状态
        // （可选）几秒后自动清除消息
        setTimeout(() => { assignmentMessage.value = null; }, 8000); // 8秒后清除
    }
};
// 组件挂载时获取数据
onMounted(() => {
    fetchStudentSummaries();
});

</script>

<style scoped>
/* --- 基础页面和内容区域样式 (复用或定义) --- */
.page-container {
    padding: 20px;
    max-width: 1300px;
    margin: auto;
}

/* 可以更宽 */
.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 25px;
    border-bottom: 1px solid #eee;
    padding-bottom: 15px;
}

.page-header h1 {
    margin: 0;
    font-size: 1.8em;
}

.back-link {
    text-decoration: none;
    color: #eff4f9;
    font-size: 0.95em;
}

.content-area {
    background-color: #fff;
    padding: 25px;
    border-radius: 8px;
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.08);
}

/* --- 加载、错误、无数据消息 --- */
.loading-message,
.error-message,
.no-data-message {
    text-align: center;
    padding: 30px;
    color: #666;
    font-size: 1.1em;
}

.error-message {
    color: #dc3545;
    background-color: #f8d7da;
    border: 1px solid #f5c6cb;
    border-radius: 5px;
}

/* --- 表格样式 (类似 AdminAnnouncements) --- */
.students-table {
    width: 100%;
    border-collapse: collapse;
    margin-top: 20px;
    font-size: 0.9em;
    /* 可以稍微小一点显示更多信息 */
    table-layout: fixed;
    /* 固定布局，防止内容撑开列 */
}

.students-table th,
.students-table td {
    border: 1px solid #e0e0e0;
    padding: 10px 12px;
    /* 减少一点内边距 */
    text-align: left;
    vertical-align: middle;
    white-space: nowrap;
    /* 默认不换行 */
    overflow: hidden;
    /* 隐藏溢出内容 */
    text-overflow: ellipsis;
    /* 显示省略号 */
}

.students-table th {
    background-color: #f8f9fa;
    font-weight: 600;
    color: #495057;
    position: sticky;
    /* 表头固定 */
    top: 0;
    /* 距离顶部0 */
    z-index: 1;
    /* 确保在滚动时在上方 */
}

.students-table tbody tr:nth-child(odd) {
    background-color: #fdfdfd;
}

.students-table tbody tr:hover {
    background-color: #f1f1f1;
}

/* 特定列样式 */
.students-table td.factors-cell {
    white-space: normal;
    /* 允许偏好因素换行 */
    font-size: 0.85em;
    /* 可以让换行内容小一点 */
}

.assignment-controls {
    margin-bottom: 25px;
    padding: 15px;
    background-color: #f0f5ff;
    /* 淡蓝色背景 */
    border: 1px solid #cce0ff;
    border-radius: 5px;
    display: flex;
    align-items: center;
    /* 垂直居中 */
    gap: 15px;
    /* 元素间距 */
    flex-wrap: wrap;
    /* 允许换行 */
}

.run-button {
    background-color: #dc3545;
    /* 红色表示潜在的破坏性操作 */
    color: white;
    padding: 10px 18px;
    border: none;
    border-radius: 5px;
    cursor: pointer;
    font-size: 1.05em;
    transition: background-color 0.2s, opacity 0.2s;
    white-space: nowrap;
    /* 防止按钮文字换行 */
}

.run-button:hover:not(:disabled) {
    background-color: #bb2d3b;
    /* 悬停加深 */
}

.run-button:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

.run-button i {
    /* 加载图标 */
    margin-right: 6px;
}

.assignment-message {
    margin: 0;
    /* 重置外边距 */
    padding: 8px 12px;
    border-radius: 4px;
    font-size: 0.95em;
    flex-grow: 1;
    /* 占据剩余空间 */
    text-align: center;
    /* 消息居中 */
}

.assignment-message.success {
    background-color: #d1e7dd;
    /* 成功消息绿色背景 */
    color: #0f5132;
    border: 1px solid #badbcc;
}

.assignment-message.error {
    background-color: #f8d7da;
    /* 失败消息红色背景 */
    color: #842029;
    border: 1px solid #f5c2c7;
}

hr {
    /* 分隔线样式 */
    margin-top: 25px;
    margin-bottom: 25px;
    border: none;
    border-top: 1px solid #eee;
}
</style>