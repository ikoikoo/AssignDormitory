# 🎓宿舍分配系统 (Dormitory Assignment System)

基于 Spring Boot + Vue 3 + Python K-Means 的智能宿舍分配系统。
本项目旨在解决高校宿舍分配中因学生性格（MBTI）、生活习惯差异导致的人际冲突。通过构建一个全栈管理系统，利用 K-Means 聚类算法 实现人群画像的精准匹配，并结合 Dijkstra 贪心策略 优化物理空间的分配逻辑。

## 📌核心功能

### 多维数据采集：基于 Vue3 开发的响应式问卷，涵盖 MBTI 性格测试、作息时间、卫生习惯及社交偏好。

### 智能化分配算法

 - 特征工程：应用 One-Hot 编码处理定性数据。
 - 聚类分析：利用 Python (Scikit-learn) 实现 K-Means 模型，并通过肘部法则动态调优。
 - 路径优化：引入 Dijkstra 思想进行宿舍物理位置的贪心匹配。

### 全栈架构实现

 - 前端：Vue 3 + Element Plus + Axios。
 - 后端：Spring Boot 框架提供 RESTful API 支持。
 - 计算层：Java 异步调用 Python 脚本实现跨语言算法集成。
 - 数据库：MySQL 实现 6 张核心业务表的数据闭环。

## 🛠️ 技术架构

graph LR
  A[Vue3 前端] -- JSON --> B[SpringBoot 后端]
  B -- 数据存储 --> C[(MySQL)]
  B -- 异步驱动 --> D[Python 算法引擎]
  D -- 聚类结果 --> B

## 环境要求

- JDK 21+
- Maven 3.9+（或使用项目自带的 `mvnw`）
- Node.js 18+
- MySQL 8.0+
- Python 3.10+（需安装依赖：`pip install -r backend/scripts/requirements.txt`）

## 🚀快速开始

### 1. 数据库

```sql
CREATE DATABASE my_project_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

首次启动后端时，JPA 会自动建表，`BatchDataInitializer` 会自动插入 50 条测试数据和管理员账号。

默认管理员账号：`admin` / `adminpass`

### 2. 后端配置

复制配置模板并按本机情况填写：

```bash
cp backend/src/main/resources/application-local.properties.example \
   backend/src/main/resources/application-local.properties
```

编辑 `application-local.properties`，至少填写：

```properties
spring.datasource.password=你的MySQL密码
app.python.interpreter.path=python   # 或 python3，或 Python 完整路径
app.python.script.path=scripts/dorm_assignment.py
app.data.temp.dir=backend/temp_data
```

### 3. 启动后端

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

后端运行在 `http://localhost:8080`。

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端运行在 `http://localhost:5173`（Vite 默认端口）。

## 📂项目结构

```
├── backend/                  Spring Boot 后端
│   ├── scripts/
│   │   ├── dorm_assignment.py   K-Means 分配脚本
│   │   └── requirements.txt     Python 依赖
│   ├── src/main/resources/
│   │   ├── application.properties          公共配置（提交到 git）
│   │   └── application-local.properties.example  本地配置模板
│   └── temp_data/            运行时临时 CSV（已被 .gitignore 排除）
└── frontend/                 Vue 3 前端
```

## 💡环境变量（可选）

也可以通过环境变量覆盖配置，无需 `application-local.properties`：

| 变量名 | 说明 | 默认值 |
|---|---|---|
| `DB_USERNAME` | MySQL 用户名 | `root` |
| `DB_PASSWORD` | MySQL 密码 | `root` |
| `JWT_SECRET` | JWT 签名密钥（生产环境必须修改） | 内置默认值 |
| `PYTHON_PATH` | Python 解释器路径 | `python` |
| `PYTHON_SCRIPT_PATH` | 分配脚本路径 | `scripts/dorm_assignment.py` |
| `TEMP_DATA_DIR` | 临时文件目录 | `temp_data` |


 
