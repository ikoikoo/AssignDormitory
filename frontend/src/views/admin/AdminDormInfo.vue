<template>
    <div class="page-container">
        <header class="page-header">
            <h1>宿舍信息查看 (管理员)</h1>
            <router-link to="/dashboard" class="back-link">返回个人主页</router-link>
        </header>

        <div class="content-area">
            <p>这里显示所有已提交宿舍习惯问卷的学生信息及得分。</p>
            <!-- 可以添加搜索或筛选控件 -->
            <!-- <input type="text" v-model="searchTerm" placeholder="搜索用户名..."> -->

            <div v-if="loading" class="loading-message">正在加载数据...</div>
            <div v-else-if="error" class="error-message">加载失败: {{ error }}</div>
            <div v-else-if="dormResults.length === 0" class="no-data-message">暂无宿舍习惯数据。</div>

            <table v-else class="results-table">
                <thead>
                    <tr>
                        <th>用户ID</th>
                        <th>用户名</th> <!-- 需要后端关联查询 -->
                        <th>作息分</th>
                        <th>卫生分</th>
                        <th>社交分</th>
                        <th>偏好因素</th>
                        <th>参与试点</th>
                        <th>提交时间</th>
                    </tr>
                </thead>
                <tbody>
                    <!-- 在这里可以添加 v-for 循环过滤后的数据 (如果做了搜索) -->
                    <tr v-for="result in dormResults" :key="result.id">
                        <td>{{ result.user?.id || 'N/A' }}</td> <!-- 使用可选链操作符 -->
                        <td>{{ result.user?.username || 'N/A' }}</td> <!-- 显示用户名 -->
                        <td>{{ result.routineScore }}</td>
                        <td>{{ result.hygieneScore }}</td>
                        <td>{{ result.socialScore }}</td>
                        <td>{{ result.preferenceFactors || '未选' }}</td>
                        <td>{{ result.pilotParticipation || '未知' }}</td>
                        <td>{{ formatDateTime(result.submissionTimestamp) }}</td>
                    </tr>
                </tbody>
            </table>
            <!-- 可以添加分页控件 -->
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import apiClient from '@/api/axios';
import { format } from 'date-fns'; // 或其他日期工具

// --- State ---
const dormResults = ref([]);
const loading = ref(true);
const error = ref(null);
// const searchTerm = ref(''); // 用于搜索功能

// --- Methods ---
const fetchAllDormResults = async () => {
    loading.value = true;
    error.value = null;
    try {
        // 调用管理员获取所有结果的接口
        const response = await apiClient.get('/api/admin/dorm-info/all'); // 确认后端路径正确
        // 后端返回的 DormHabitsResult 应该包含了关联的 User 对象的部分信息 (如 id, username)
        dormResults.value = response.data || [];
        console.log("获取到的宿舍数据:", dormResults.value); // 调试用，检查 user 字段是否存在
    } catch (err) {
        console.error('获取所有宿舍习惯数据失败:', err);
        error.value = err.response?.data?.message || err.message || '无法加载数据';
    } finally {
        loading.value = false;
    }
};

// 格式化日期时间
const formatDateTime = (dateTimeString) => {
    if (!dateTimeString) return 'N/A';
    try {
        return format(new Date(dateTimeString), 'yyyy-MM-dd HH:mm');
    } catch (e) {
        return dateTimeString;
    }
};

// --- Computed Properties (for filtering/sorting - 可选) ---
// const filteredResults = computed(() => {
//   if (!searchTerm.value) {
//     return dormResults.value;
//   }
//   const lowerSearch = searchTerm.value.toLowerCase();
//   return dormResults.value.filter(result =>
//     result.user?.username?.toLowerCase().includes(lowerSearch)
//     // 添加其他搜索条件...
//   );
// });


// --- Lifecycle Hook ---
onMounted(() => {
    fetchAllDormResults();
});
</script>

<style scoped>
/* 引入或定义通用页面样式 */
.page-container {
    padding: 20px;
    max-width: 1200px;
    margin: auto;
}

/* 页面可以宽一点 */
.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    border-bottom: 1px solid #eee;
    padding-bottom: 10px;
}

.page-header h1 {
    margin: 0;
}

.back-link {
    text-decoration: none;
    color: #007bff;
}

.content-area {
    background-color: #fff;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.content-area>p:first-child {
    color: #666;
    margin-bottom: 20px;
}

/* 表格样式 */
.results-table {
    width: 100%;
    border-collapse: collapse;
    margin-top: 20px;
}

.results-table th,
.results-table td {
    border: 1px solid #ddd;
    padding: 10px 12px;
    text-align: left;
}

.results-table th {
    background-color: #f2f2f2;
    font-weight: bold;
    white-space: nowrap;
    /* 防止表头换行 */
}

.results-table td {
    font-size: 0.95em;
}

/* 通用消息样式 */
.loading-message,
.error-message,
.no-data-message {
    text-align: center;
    padding: 20px;
    margin-top: 20px;
    color: #666;
    border: 1px dashed #ccc;
    border-radius: 4px;
}

.error-message {
    color: #dc3545;
    background-color: #f8d7da;
    border-color: #f5c6cb;
}
</style>