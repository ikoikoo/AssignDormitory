<template>
    <div class="dashboard-layout">
        <aside class="sidebar">
            <div class="sidebar-header">
                <h3>宿舍系统</h3>
            </div>
            <nav class="sidebar-nav">
                <router-link :to="{ name: 'Dashboard' }" active-class="active-link" exact>首页</router-link>
                <router-link :to="{ name: 'MbtiForm' }" active-class="active-link">MBTI问卷</router-link>
                <router-link :to="{ name: 'DormHabitsForm' }" active-class="active-link">宿舍习惯问卷</router-link>
                <router-link :to="{ name: 'Announcements' }" active-class="active-link">公告</router-link>
                <router-link :to="{ name: 'PersonalInfo' }" active-class="active-link">个人信息</router-link>
            </nav>
            <!-- === 管理员专属链接 === -->
            <router-link v-if="isAdmin" to="/admin/dorms" class="nav-link admin-link">
                <div class="nav-item">宿舍信息管理</div>
            </router-link>
            <div class="sidebar-footer">
                <button @click="handleLogout" class="logout-button">退出登录</button>
            </div>
        </aside>
        <main class="main-content">
            <!-- Nested routes will be rendered here -->
            <router-view />
        </main>
    </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const studentName = ref('同学');

// --- 获取用户信息的辅助函数 (复用或单独实现) ---
const getUserInfo = () => {
    const userInfoString = localStorage.getItem('user-info');
    if (!userInfoString) return null;
    try { return JSON.parse(userInfoString); } catch (e) { return null; }
};

// --- 添加 isAdmin 计算属性 ---
const isAdmin = computed(() => {
    const userInfo = getUserInfo();
    return userInfo && userInfo.roles && userInfo.roles.includes('ROLE_ADMIN');
});

const handleLogout = () => {
    console.log('执行退出登录');
    // 1. 清除认证信息 (Token 和 User Info)
    localStorage.removeItem('user-token');
    localStorage.removeItem('user-info');
    localStorage.removeItem('user-id');
    // 2. 跳转回登录页
    // 使用 replace 通常更好，这样用户按浏览器后退键不会回到刚才的页面
    router.replace({ name: 'Login' });
};

onMounted(() => {
    const userInfo = getUserInfo();
    if (userInfo) {
        studentName.value = userInfo.name || userInfo.username || '同学';
    }
});

</script>

<style scoped>
/* You can also put this in src/assets/styles/dashboard.css and import it */
.dashboard-layout {
    display: flex;
    min-height: 100vh;
}

.sidebar {
    width: 220px;
    background-color: #2c3e50;
    /* Dark sidebar */
    color: #ecf0f1;
    /* Light text */
    display: flex;
    flex-direction: column;
    flex-shrink: 0;
    /* Prevent sidebar from shrinking */
}

.sidebar-header {
    padding: 20px;
    text-align: center;
    border-bottom: 1px solid #34495e;
}

.sidebar-header h3 {
    margin: 0;
    color: #ffffff;
}

.sidebar-nav {
    flex-grow: 1;
    padding-top: 15px;
}

.sidebar-nav a {
    display: block;
    padding: 12px 20px;
    color: #bdc3c7;
    /* Lighter grey text */
    text-decoration: none;
    transition: background-color 0.2s ease, color 0.2s ease;
    font-size: 15px;
}

.sidebar-nav a:hover {
    background-color: #34495e;
    /* Darker background on hover */
    color: #ffffff;
}

/* Style for the active route link */
.sidebar-nav a.active-link {
    background-color: #1abc9c;
    /* Teal color for active link */
    color: #ffffff;
    font-weight: bold;
}

.sidebar-footer {
    padding: 20px;
    border-top: 1px solid #34495e;
}

.logout-button {
    width: 100%;
    padding: 10px;
    background-color: #e74c3c;
    /* Red */
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 14px;
    transition: background-color 0.3s ease;
}

.logout-button:hover {
    background-color: #c0392b;
}

.main-content {
    flex-grow: 1;
    /* Take remaining space */
    padding: 30px;
    background-color: #f4f7f6;
    /* Light background for content */

    background-image: url("../assets/gduf.jpg");
    opacity: 0.8;
    /* 设置透明度 */
    background-size: 100% 100%;
    overflow-y: auto;
    /* Allow content to scroll if needed */
}

.nav-item {
    padding: 20px;
    color: #dce2e6;
    /* text-align: center; */
}

.nav-link {
    text-decoration: none;
}

.nav-item:hover {
    background-color: #34495e;
    /* Darker background on hover */
    color: #ffffff;
    text-decoration: none;
}
</style>