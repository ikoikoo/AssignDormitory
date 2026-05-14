import axios from 'axios'
import router from '@/router'

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('user-token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('user-token')
      localStorage.removeItem('user-info')
      if (router.currentRoute.value.name !== 'Login') {
        router.push({ name: 'Login', query: { sessionExpired: 'true' } })
      }
      return Promise.reject(new Error('会话已过期或无效，请重新登录'))
    }
    return Promise.reject(error)
  },
)

export default apiClient
