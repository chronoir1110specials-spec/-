<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userApi } from '@/api/user'

const router = useRouter()
const loading = ref(false)
const form = reactive({ username: '', password: '', confirmPassword: '' })

async function submit() {
  if (!form.username || !form.password || form.password !== form.confirmPassword) {
    ElMessage.warning('请确认用户名和两次密码一致')
    return
  }
  loading.value = true
  try {
    await userApi.register(form)
    ElMessage.success('注册成功，请登录')
    router.replace('/login')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <el-card class="auth-card" shadow="never">
      <h1>注册账号</h1>
      <p class="muted">创建学生账号后完善求职画像</p>
      <el-form class="mt-6" :model="form" label-position="top" @keyup.enter="submit">
        <el-form-item label="用户名"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" type="password" show-password /></el-form-item>
        <el-form-item label="确认密码"><el-input v-model="form.confirmPassword" type="password" show-password /></el-form-item>
        <el-button class="w-full" type="primary" :loading="loading" @click="submit">注册</el-button>
      </el-form>
      <div class="mt-4 text-center text-sm"><RouterLink to="/login">已有账号？登录</RouterLink></div>
    </el-card>
  </div>
</template>

<style scoped>
.auth-page { min-height: 100vh; display: grid; place-items: center; background: #0f172a; }
.auth-card { width: min(420px, calc(100vw - 32px)); }
h1 { font-size: 28px; font-weight: 700; color: #f8fafc; }
</style>
