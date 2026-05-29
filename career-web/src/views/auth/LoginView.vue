<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

async function submit() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    await userStore.login(form.username, form.password)
    ElMessage.success('登录成功')
    router.replace(String(route.query.redirect || '/home'))
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <el-card class="auth-card" shadow="never">
      <h1>Career Agent</h1>
      <p class="muted">登录后使用就业辅导工作台</p>
      <el-form class="mt-6" :model="form" label-position="top" @keyup.enter="submit">
        <el-form-item label="用户名"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" type="password" show-password /></el-form-item>
        <el-button class="w-full" type="primary" :loading="loading" @click="submit">登录</el-button>
      </el-form>
      <div class="mt-4 text-center text-sm"><RouterLink to="/register">没有账号？注册</RouterLink></div>
    </el-card>
  </div>
</template>

<style scoped>
.auth-page { min-height: 100vh; display: grid; place-items: center; background: linear-gradient(120deg, #eef3ff 0%, #f5f7fa 100%); }
.auth-card { width: min(420px, calc(100vw - 32px)); border-radius: 14px; }
h1 { font-size: 26px; font-weight: 800; color: #1f2329; }
</style>
