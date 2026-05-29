import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { UserProfile } from '@/types'
import { userApi } from '@/api/user'
import { getToken, setToken } from '@/api/client'

function emptyProfile(): UserProfile {
  return {
    school: '',
    major: '',
    grade: '',
    targetPosition: '',
    targetCity: '',
    skillTags: '',
    projectTags: '',
    jobStage: '',
    notifications: { jobAlert: true, courseReminder: true, interviewFeedback: true }
  }
}

export const useUserStore = defineStore('user', () => {
  const profile = ref<UserProfile>(emptyProfile())
  const token = ref<string>(getToken())
  const roles = ref<string[]>((localStorage.getItem('career-roles') || 'user').split(',').filter(Boolean))

  async function loadProfile() {
    if (!token.value) return null
    const data = await userApi.profile().catch(() => null)
    profile.value = { ...emptyProfile(), ...(data || {}) }
    return profile.value
  }

  async function updateProfile(nextProfile: UserProfile) {
    await userApi.updateProfile(nextProfile)
    profile.value = { ...emptyProfile(), ...nextProfile }
  }

  async function login(username: string, password: string) {
    const t = await userApi.login(username, password)
    token.value = t
    roles.value = username === 'admin' ? ['admin'] : ['user']
    await loadProfile()
    return t
  }

  async function logout() {
    await userApi.logout()
    token.value = ''
    roles.value = ['user']
    profile.value = emptyProfile()
    setToken('')
  }

  function isLoggedIn() {
    return Boolean(token.value)
  }

  function hasRole(role: string) {
    return roles.value.includes(role)
  }

  return { profile, token, roles, loadProfile, updateProfile, login, logout, isLoggedIn, hasRole }
})
