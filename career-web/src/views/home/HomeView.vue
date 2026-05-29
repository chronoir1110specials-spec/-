<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { adminApi, type AdminStats } from '@/api/admin'
import { chatApi } from '@/api/chat'
import { getToken } from '@/api/client'
import type { ChatSession } from '@/types'

const router = useRouter()
const loggedIn = computed(() => !!getToken())

const stats = ref<AdminStats | null>(null)
const recentSessions = ref<ChatSession[]>([])

const services = [
  { label: '简历优化', desc: '结构化评分 · 应届/国企双模式改写建议', path: '/resume/upload', icon: '📄' },
  { label: '岗位分析', desc: '解析 JD · 技能匹配 · 面试重点', path: '/jobs', icon: '🎯' },
  { label: '智能对话', desc: '多轮就业咨询 · 流式回答', path: '/chat', icon: '💬' },
  { label: '知识库问答', desc: '政策/流程检索 · 来源可追溯', path: '/knowledge', icon: '📚' }
]

const statCards = [
  { key: 'totalUsers', label: '注册用户' },
  { key: 'totalSessions', label: '对话会话' },
  { key: 'totalMessages', label: '消息总数' },
  { key: 'totalDocuments', label: '知识文档' }
]

function go(path: string) {
  if (loggedIn.value) router.push(path)
  else router.push({ path: '/login', query: { redirect: path } })
}

function start() {
  go('/chat')
}

onMounted(async () => {
  // 仅登录后加载个人/统计数据，匿名访问不触发需鉴权接口
  if (!loggedIn.value) return
  try { stats.value = await adminApi.stats() } catch { /* ignore */ }
  try { recentSessions.value = (await chatApi.sessions()).slice(0, 6) } catch { /* ignore */ }
})
</script>

<template>
  <div class="page-grid">
    <!-- Hero：展示核心定位 + 开始 -->
    <section class="hero">
      <div class="hero-text">
        <h1>大学生就业辅导 Agent</h1>
        <p>简历优化、岗位分析、就业知识库问答与智能对话，一站式 AI 求职助手。</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" round @click="start">开始使用</el-button>
          <el-button v-if="!loggedIn" size="large" round @click="router.push('/register')">注册账号</el-button>
        </div>
      </div>
    </section>

    <!-- 核心服务 -->
    <div>
      <div class="section-label">核心服务</div>
      <div class="card-grid">
        <div v-for="s in services" :key="s.path" class="service-card" @click="go(s.path)">
          <div class="service-icon">{{ s.icon }}</div>
          <div class="service-name">{{ s.label }}</div>
          <div class="muted service-desc">{{ s.desc }}</div>
        </div>
      </div>
    </div>

    <!-- 登录后：统计 + 最近会话 -->
    <template v-if="loggedIn">
      <div class="card-grid">
        <div v-for="card in statCards" :key="card.key" class="panel">
          <div class="muted">{{ card.label }}</div>
          <div class="mt-3"><span class="metric-value">{{ stats?.[card.key as keyof AdminStats] ?? 0 }}</span></div>
        </div>
      </div>

      <section class="panel">
        <div class="panel-title">最近会话</div>
        <el-empty v-if="recentSessions.length === 0" description="还没有对话，去开启一次就业辅导" />
        <el-timeline v-else>
          <el-timeline-item
            v-for="session in recentSessions"
            :key="session.id"
            :timestamp="session.createTime || ''"
            type="primary"
          >
            <strong class="link" @click="router.push('/chat')">{{ session.title || `会话 ${session.id}` }}</strong>
          </el-timeline-item>
        </el-timeline>
      </section>
    </template>
  </div>
</template>

<style scoped>
.hero {
  background: linear-gradient(120deg, #eef3ff 0%, #f7f9fc 100%);
  border: 1px solid #e9ecf2;
  border-radius: 16px;
  padding: 48px 40px;
}
.hero-text h1 { font-size: 30px; font-weight: 800; color: #1f2329; margin-bottom: 12px; }
.hero-text p { color: #5b6271; font-size: 16px; max-width: 640px; line-height: 1.7; }
.hero-actions { margin-top: 24px; display: flex; gap: 12px; flex-wrap: wrap; }

.section-label { font-size: 15px; font-weight: 700; color: #1f2329; margin-bottom: 14px; }

.service-card {
  background: #ffffff;
  border: 1px solid #e9ecf2;
  border-radius: 12px;
  padding: 22px;
  cursor: pointer;
  transition: all .18s ease;
  box-shadow: 0 1px 2px rgba(16, 24, 40, 0.04);
}
.service-card:hover { border-color: #4f7cff; transform: translateY(-2px); box-shadow: 0 6px 18px rgba(79, 124, 255, 0.12); }
.service-icon { font-size: 26px; }
.service-name { font-weight: 700; font-size: 16px; color: #1f2329; margin: 10px 0 6px; }
.service-desc { font-size: 13px; line-height: 1.6; }

.link { color: #4f7cff; cursor: pointer; }
</style>
