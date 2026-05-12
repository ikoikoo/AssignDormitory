import axios from 'axios'
import router from '@/router'

const apiClient = axios.create({
  // --- 确保这里的 baseURL 指向 Spring Boot 后端 ---
  baseURL: 'http://localhost:8080/api',
  // --- ---

  headers: {
    'Content-Type': 'application/json',
  },
})

// 添加响应拦截器 - 例如，处理 401 未授权错误，自动跳转登录页
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('user-token') // 从 localStorage 获取 Token
    if (token) {
      // 如果 Token 存在，添加到 Authorization 请求头
      config.headers.Authorization = `Bearer ${token}`
      console.log('附加 Token 到请求头:', config.headers.Authorization) // 调试日志
    } else {
      console.log('未找到 Token，不附加到请求头') // 调试日志
    }
    return config // 返回修改后的配置
  },
  (error) => {
    // 处理请求错误
    return Promise.reject(error)
  },
)

apiClient.interceptors.response.use(
  (response) => {
    // 对响应数据做点什么
    return response
  },
  (error) => {
    // 对响应错误做点什么
    if (error.response && error.response.status === 401) {
      // 如果收到 401 未授权错误 (通常是 Token 无效或过期)
      console.error(
        'Axios Interceptor: Received 401 Unauthorized. Clearing token and redirecting to login.',
      )
      // 清除本地存储
      localStorage.removeItem('user-token')
      localStorage.removeItem('user-info') // 可能也需要清除用户信息
      // 跳转到登录页 (如果 router 可用)
      if (router.currentRoute.value.name !== 'Login') {
        // 避免重复跳转
        router.push({ name: 'Login', query: { sessionExpired: 'true' } }) // 可以带个参数提示会话过期
      }
      // 返回一个被拒绝的 Promise，阻止后续的 .then() 或 .catch() 处理这个特定错误
      return Promise.reject(new Error('会话已过期或无效，请重新登录'))
    } else if (error.response && error.response.status === 403) {
      // 如果收到 403 Forbidden (权限不足)
      console.error('Axios Interceptor: Received 403 Forbidden.')
      // 可以选择跳转到无权限页面，或者仅仅是抛出错误让调用者处理
      // return Promise.reject(new Error('您没有权限执行此操作'));
    }
    // 其他错误继续抛出
    return Promise.reject(error)
  },
)
// --- ---

export default apiClient
