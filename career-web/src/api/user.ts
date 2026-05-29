import type { UserProfile } from '@/types'
import { get, post, put, setToken } from './client'

interface LoginResponse {
  access_token?: string
  token?: string
  expires_in?: number
}

export interface RegisterPayload {
  username: string
  password: string
  confirmPassword?: string
}

export const userApi = {
  profile: (): Promise<UserProfile> => get<UserProfile>('/profile/get'),

  updateProfile: (profile: UserProfile): Promise<boolean> => put<boolean>('/profile/update', profile),

  saveProfile: (profile: UserProfile): Promise<boolean> => post<boolean>('/profile/save', profile),

  login: async (username: string, password: string): Promise<string> => {
    const data = await post<LoginResponse>('/auth/login', { username, password })
    const token = data?.access_token || data?.token || ''
    if (token) {
      setToken(token)
      localStorage.setItem('career-username', username)
      localStorage.setItem('career-roles', username === 'admin' ? 'admin' : 'user')
    }
    return token
  },

  register: (payload: RegisterPayload): Promise<unknown> => post('/auth/register', payload),

  logout: async (): Promise<void> => {
    try {
      await post('/auth/logout', {})
    } finally {
      setToken('')
      localStorage.removeItem('career-roles')
    }
  }
}
