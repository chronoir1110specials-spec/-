import axios, { AxiosError, type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'

const TOKEN_KEY = 'career-token'

export function getToken(): string {
  return localStorage.getItem(TOKEN_KEY) || import.meta.env.VITE_DEV_TOKEN || ''
}

export function setToken(token: string) {
  if (token) localStorage.setItem(TOKEN_KEY, token)
  else localStorage.removeItem(TOKEN_KEY)
}

export const http = axios.create({
  baseURL: '/api',
  timeout: 30000
})

http.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers = config.headers || {}
    ;(config.headers as Record<string, string>).Authorization = `Bearer ${token}`
  }
  return config
})

interface ApiResult<T> {
  code: number
  msg?: string
  data?: T
}

function handleUnauthorized() {
  setToken('')
  if (!window.location.pathname.startsWith('/login')) {
    window.location.href = `/login?redirect=${encodeURIComponent(window.location.pathname + window.location.search)}`
  }
}

http.interceptors.response.use(
  (response) => {
    const body = response.data as ApiResult<unknown> | unknown
    if (body && typeof body === 'object' && 'code' in (body as Record<string, unknown>)) {
      const r = body as ApiResult<unknown>
      if (r.code === 401) handleUnauthorized()
      if (r.code !== 200 && r.code !== 0) {
        ElMessage.error(r.msg || '请求失败')
        return Promise.reject(new Error(r.msg || '请求失败'))
      }
    }
    return response
  },
  (error: AxiosError) => {
    if (error.response?.status === 401) handleUnauthorized()
    const msg = (error.response?.data as ApiResult<unknown> | undefined)?.msg || error.message
    ElMessage.error(msg || '网络异常')
    return Promise.reject(error)
  }
)

export async function get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
  const res = await http.get<ApiResult<T>>(url, config)
  return (res.data?.data ?? (res.data as unknown)) as T
}

export async function post<T>(url: string, body?: unknown, config?: AxiosRequestConfig): Promise<T> {
  const res = await http.post<ApiResult<T>>(url, body, config)
  return (res.data?.data ?? (res.data as unknown)) as T
}

export async function put<T>(url: string, body?: unknown, config?: AxiosRequestConfig): Promise<T> {
  const res = await http.put<ApiResult<T>>(url, body, config)
  return (res.data?.data ?? (res.data as unknown)) as T
}

export async function del<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
  const res = await http.delete<ApiResult<T>>(url, config)
  return (res.data?.data ?? (res.data as unknown)) as T
}

export async function postRaw<T>(url: string, body?: unknown, config?: AxiosRequestConfig): Promise<ApiResult<T>> {
  const res = await http.post<ApiResult<T>>(url, body, config)
  return res.data
}
