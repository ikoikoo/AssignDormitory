<template>
    <div class="dashboard-container">
      <header class="dashboard-header">
        <h1>欢迎回来, {{ studentName }} !</h1>
        <button @click="logout" class="logout-button">退出登录</button>
      </header>
      <p>这里是你的个人主页。请选择您要进行的操作：</p>
      <nav class="dashboard-nav">
        <router-link to="/mbti" class="nav-link">
          <div class="nav-item">填写 MBTI 问卷</div>
        </router-link>
        <router-link to="/dorm-habits" class="nav-link">
           <div class="nav-item">填写宿舍习惯问卷</div>
        </router-link>
        <router-link to="/announcements" class="nav-link">
           <div class="nav-item">查看公告</div>
        </router-link>
        <router-link to="/profile" class="nav-link">
           <div class="nav-item">个人信息管理</div>
        </router-link>
      </nav>
      <main class="dashboard-content">
        <!-- 未来可以在这里直接显示一些摘要信息 -->
        <p><i>选择上方链接以继续操作。</i></p>
      </main>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue';
  import { useRouter } from 'vue-router';
  
  const router = useRouter();
  const studentName = ref('同学'); // 默认值
  
  onMounted(() => {
    // 尝试从 localStorage 获取用户信息
    const userInfo = localStorage.getItem('user-info');
    if (userInfo) {
      try {
        const parsedInfo = JSON.parse(userInfo);
        studentName.value = parsedInfo.name || parsedInfo.username || '同学'; // 优先用 name
      } catch (e) {
        console.error("解析用户信息失败:", e);
      }
    }
  });
  
  
  const logout = () => {
    console.log('执行退出登录');
    // 清除认证信息
    localStorage.removeItem('user-token');
    localStorage.removeItem('user-info');
    // 跳转回登录页
    router.push({ name: 'Login' });
  };
  </script>
  
  <style scoped>
  .dashboard-container {
    padding: 20px;
    background-color: #f8f9fa;
    border-radius: 8px;
  }
  
  .dashboard-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 15px;
    border-bottom: 1px solid #eee;
  }
  
  .dashboard-header h1 {
    margin: 0;
    color: #343a40;
    font-size: 1.8em;
  }
  
  .logout-button {
    padding: 8px 15px;
    background-color: #dc3545;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 0.9em;
    transition: background-color 0.3s ease;
  }
  
  .logout-button:hover {
    background-color: #c82333;
  }
  
  .dashboard-nav {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); /* 响应式网格 */
    gap: 15px; /* 网格间距 */
    margin-bottom: 30px;
  }
  
  .nav-link {
    text-decoration: none;
  }
  
  .nav-item {
    display: block;
    padding: 20px;
    background-color: #ffffff;
    color: #007bff;
    text-align: center;
    border: 1px solid #dee2e6;
    border-radius: 5px;
    transition: all 0.3s ease;
    box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  }
  
  .nav-item:hover {
    background-color: #007bff;
    color: white;
    transform: translateY(-3px);
    box-shadow: 0 4px 8px rgba(0,0,0,0.1);
  }
  
  .dashboard-content {
    margin-top: 20px;
    background-color: #fff;
    padding: 20px;
    border-radius: 5px;
    border: 1px solid #eee;
  }
  .dashboard-content i {
      color: #6c757d;
  }
  </style>