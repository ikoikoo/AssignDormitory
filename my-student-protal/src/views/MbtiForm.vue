<template>
    <div class="page-container">
      <header class="page-header">
        <h1>MBTI 问卷</h1>
        <router-link to="/dashboard" class="back-link">返回个人主页</router-link>
      </header>
      <div class="content-area">
        <p>这里是 MBTI 问卷的内容区域。</p>
        <!-- 在这里添加你的问卷表单 -->
        <form @submit.prevent="submitMbti">
          <!-- 示例问题 -->
          <div class="question">
            <label>问题 1: 你更倾向于？</label>
            <div>
              <input type="radio" id="q1_option1" name="q1" value="E">
              <label for="q1_option1">在社交场合中获得能量 (外向 E)</label>
            </div>
            <div>
              <input type="radio" id="q1_option2" name="q1" value="I">
              <label for="q1_option2">在独处时获得能量 (内向 I)</label>
            </div>
          </div>
          <!-- 添加更多问题... -->
          <button type="submit" class="submit-button">提交问卷</button>
        </form>
      </div>
    </div>
  </template>
  
  <script setup>
  import { useRouter } from 'vue-router';
  
  const router = useRouter();
  
  const submitMbti = () => {
    // 处理表单提交逻辑，例如发送数据到后端
    console.log('提交 MBTI 问卷...');
    // 提交后可以跳转或显示提示信息
    alert('MBTI 问卷已提交（模拟）');
    router.push('/dashboard'); // 或跳转到结果页
  };
  </script>
  
  <style scoped>
  /* 使用通用的页面样式 (可以提取到 main.css 或创建一个通用布局组件) */
  .page-container {
    padding: 20px;
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
    padding: 25px;
    border-radius: 5px;
    border: 1px solid #eee;
  }
  .question {
      margin-bottom: 20px;
  }
  .question label {
      display: block;
      font-weight: bold;
      margin-bottom: 10px;
  }
   .question div {
      margin-bottom: 8px;
  }
  .question input[type="radio"] {
      margin-right: 8px;
  }
  .submit-button {
    margin-top: 20px;
    padding: 10px 20px;
    background-color: #28a745;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 1em;
  }
  .submit-button:hover {
    background-color: #218838;
  }
  </style>