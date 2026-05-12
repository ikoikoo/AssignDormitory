<template>
    <div class="page-container">
        <header class="page-header">
            <h1>公告通知</h1>
            <router-link to="/dashboard" class="back-link">返回个人主页</router-link>
        </header>
        <div class="content-area">
            <!-- ======== 管理员操作区域 ======== -->
            <div v-if="isAdmin" class="admin-section">
                <!-- 直接导入和使用 AdminAnnouncements 组件 -->
                <!-- 监听子组件触发的 'announcement-updated' 事件，调用 refreshAnnouncements 方法 -->
                <AdminAnnouncements @announcement-updated="refreshAnnouncements" />
                <hr class="admin-divider"> <!-- 分隔线 -->
            </div>
            <!-- 只有在不是管理员时，才显示这个独立的列表 -->
            <div v-if="!isAdmin">
                <h2>公告列表</h2>
                <div v-if="loading" class="loading-message">加载中...</div>
                <div v-else-if="error" class="error-message">{{ error }}</div>
                <div v-else-if="announcements.length === 0" class="no-data-message">
                    暂无公告信息。
                </div>
                <!-- 普通用户的公告列表 -->
                <ul v-else class="announcement-list">
                    <!-- 使用 v-for 遍历 announcements ref -->
                    <li v-for="announcement in announcements" :key="announcement.id">
                        <div class="announcement-header">
                            <!-- 显示标题 -->
                            <h3 class="announcement-title">{{ announcement.title }}</h3>
                        </div>
                        <!-- 显示日期 -->
                        <p class="announcement-date">发布时间: {{ formatDateTime(announcement.publishDate) }}</p>
                        <!-- 显示内容 -->
                        <div class="announcement-content" v-html="formatContent(announcement.content)"></div>
                    </li>
                </ul>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import apiClient from '@/api/axios'; // 引入 Axios 实例
import { format } from 'date-fns'; // 引入 date-fns 用于日期格式化 (npm install date-fns)
import AdminAnnouncements from './admin/AdminAnnouncements.vue';

const announcements = ref([]);
const loading = ref(true);
const error = ref(null);

// --- 获取用户信息的辅助函数 (保持不变) ---
const getUserInfo = () => {
    const userInfoString = localStorage.getItem('user-info');
    if (!userInfoString) return null;
    try { return JSON.parse(userInfoString); } catch (e) { return null; }
};

// --- 计算属性：判断是否为管理员 ---
const isAdmin = computed(() => {
    const userInfo = getUserInfo();
    const isAdminUser = userInfo && userInfo.roles && userInfo.roles.includes('ROLE_ADMIN');
    console.log('Announcements.vue - isAdmin computed:', isAdminUser); // 添加日志
    return isAdminUser;
});

// --- (调试用) 显示用户角色 ---
const userRoles = computed(() => {
    const userInfo = getUserInfo();
    return userInfo?.roles?.join(', ') || '未登录或无角色';
});

// 获取公告数据的函数
const fetchAnnouncements = async () => {
    loading.value = true;
    error.value = null;
    try {
        console.log("Announcements.vue: Fetching announcements for non-admin view...");
        const response = await apiClient.get('/announcements'); // 确认调用 GET /api/announcements
        announcements.value = response.data || []; // 将获取的数据存入 ref
        console.log("Announcements.vue: Announcements fetched:", announcements.value);
    } catch (err) {
        console.error('获取公告列表失败 (Announcements.vue):', err);
        error.value = '无法加载公告信息，请稍后再试。';
        if (err.response && err.response.status === 404) {
            error.value = '找不到公告资源。'; // 更具体的错误
        } else if (!err.response) {
            error.value = '网络错误，请检查连接。';
        }
    } finally {
        loading.value = false;
    }
};

// 供 AdminAnnouncements 组件调用的刷新方法 ---
const refreshAnnouncements = () => {
    console.log('Announcements.vue 监听到 announcement-updated 事件');
};

// 日期时间格式化函数
const formatDateTime = (dateTimeString) => {
    if (!dateTimeString) return 'N/A';
    try {
        // 使用 date-fns (推荐)
        return format(new Date(dateTimeString), 'yyyy年MM月dd日 HH:mm');
        // 或者使用简单的内置方法 (格式可能因浏览器和地区不同)
        // return new Date(dateTimeString).toLocaleString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' });
    } catch (e) {
        console.error("日期格式化错误:", e);
        return dateTimeString; // 返回原始字符串
    }
};

// 内容格式化 (例如，将换行符转为 <br>) - 如果内容是纯文本
const formatContent = (content) => {
    if (!content) return '';
    // 如果后端存的是纯文本，将换行符替换为 <br> 标签
    // 注意：如果内容可能包含 HTML，直接使用 v-html，并确保后端做了 XSS 清理
    return content.replace(/\n/g, '<br>');
}

// 组件挂载后执行获取数据
onMounted(() => {
    fetchAnnouncements();
});
</script>

<style scoped>
/* 可以复用之前的 page-container, page-header, content-area, back-link 样式 */
@import './MbtiForm.vue?scoped=true';
/* 假设 MbtiForm.vue 定义了这些通用样式 */

.loading-message,
.error-message,
.no-data-message {
    text-align: center;
    padding: 20px;
    color: #666;
}

.error-message {
    color: #dc3545;
}

.admin-section {
    margin-bottom: 30px;
    padding-bottom: 20px;
    /* border-bottom: 1px solid #eee; */
    /* 分隔线由 hr 提供 */
}

.admin-divider {
    margin-top: 30px;
    border: none;
    border-top: 2px solid #0d6efd;
    /* 蓝色分隔线，更醒目 */
}

.announcement-list {
    list-style: none;
    padding: 0;
    margin: 0;
}

.announcement-list li {
    background-color: #f8f9fa;
    border: 1px solid #eee;
    border-radius: 5px;
    padding: 20px;
    margin-bottom: 20px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.announcement-title {
    margin-top: 0;
    margin-bottom: 10px;
    color: #0056b3;
    font-size: 1.3em;
}

.announcement-date {
    font-size: 0.9em;
    color: #6c757d;
    margin-bottom: 15px;
}

.announcement-content {
    color: #333;
    line-height: 1.7;
    white-space: pre-wrap;
    /* 保留换行和空格，如果使用 {{ content }} */
}

/* 如果使用 v-html, white-space 可能不需要 */
.announcement-content>p:first-child {
    margin-top: 0;
}

.announcement-content>p:last-child {
    margin-bottom: 0;
}
</style>