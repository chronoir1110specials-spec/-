<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Bell,
  Briefcase,
  ChatDotRound,
  DataAnalysis,
  Document,
  Fold,
  House,
  Microphone,
  Reading,
  Setting,
  Tickets,
  User,
  Expand,
  Collection
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const collapsed = ref(false)
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const pageTitle = computed(() => String(route.meta.title ?? '就业辅导 Agent'))
const displayName = computed(() => userStore.profile.name || userStore.profile.school || localStorage.getItem('career-username') || '当前用户')
const displayTitle = computed(() => userStore.profile.title || userStore.profile.targetPosition || userStore.profile.jobStage || '求职学生')
const activeMenu = computed(() => {
  if (route.path.startsWith('/assessment')) return '/assessment'
  if (route.path.startsWith('/resume')) return '/resume/upload'
  if (route.path.startsWith('/interview')) return '/interview/room'
  if (route.path.startsWith('/jobs')) return '/jobs'
  if (route.path.startsWith('/admin/logs')) return '/admin/logs'
  if (route.path.startsWith('/admin/model')) return '/admin/model'
  return route.path
})

const navItems = computed(() => {
  const items = [
    { index: '/home', label: '首页', icon: House },
    { index: '/assessment', label: '职业测评', icon: DataAnalysis },
    { index: '/resume/upload', label: '简历管理', icon: Document },
    { index: '/chat', label: '智能对话', icon: ChatDotRound },
    { index: '/interview/room', label: '面试训练', icon: Microphone },
    { index: '/jobs', label: '岗位匹配', icon: Briefcase },
    { index: '/learning', label: '学习路径', icon: Reading },
    { index: '/profile', label: '个人中心', icon: User }
  ]
  if (userStore.hasRole('admin')) {
    items.push(
      { index: '/knowledge', label: '知识库管理', icon: Collection },
      { index: '/admin/model', label: '模型配置', icon: Setting },
      { index: '/admin/logs', label: '运行日志', icon: Tickets }
    )
  }
  return items
})

async function logout() {
  await userStore.logout()
  router.replace('/login')
}
</script>

<template>
  <el-container class="app-shell">
    <el-aside :width="collapsed ? '72px' : '236px'" class="sidebar">
      <div class="brand" :class="{ compact: collapsed }">
        <div class="brand-mark">CA</div>
        <div v-if="!collapsed" class="brand-text">
          <strong>Career Agent</strong>
          <span>就业辅导工作台</span>
        </div>
      </div>

      <el-menu router :default-active="activeMenu" :collapse="collapsed" background-color="#121827" text-color="#aab4c8" active-text-color="#ffffff" class="nav-menu">
        <el-menu-item v-for="item in navItems" :key="item.index" :index="item.index">
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>{{ item.label }}</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="topbar">
        <div class="topbar-left">
          <el-button circle :icon="collapsed ? Expand : Fold" @click="collapsed = !collapsed" />
          <div>
            <div class="page-title">{{ pageTitle }}</div>
            <div class="page-subtitle">AI 辅助职业规划、求职材料和面试训练</div>
          </div>
        </div>
        <div class="topbar-right">
          <el-badge :value="3" class="notice"><el-button circle :icon="Bell" /></el-badge>
          <el-avatar>{{ displayName.slice(0, 1) }}</el-avatar>
          <div class="user-meta">
            <strong>{{ displayName }}</strong>
            <span>{{ displayTitle }}</span>
          </div>
          <el-button link @click="logout">退出</el-button>
        </div>
      </el-header>

      <el-main class="content"><RouterView /></el-main>
    </el-container>
  </el-container>
</template>
