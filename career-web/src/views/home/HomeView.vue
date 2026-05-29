<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api/admin'
import type { AdminStats } from '@/api/admin'
import { chatApi } from '@/api/chat'
import type { ChatSession } from '@/types'

const stats = ref<AdminStats | null>(null)
const loading = ref(false)
const recentSessions = ref<ChatSession[]>([])

const statCards = [
  { key: 'totalUsers', label: '注册用户', type: 'success' },
  { key: 'totalSessions', label: '对话会话', type: 'primary' },
  { key: 'totalMessages', label: '消息总数', type: 'info' },
  { key: 'totalDocuments', label: '知识文档', type: 'warning' }
]

const quickEntries = [
  { label: '智能对话', desc: '多轮就业咨询', path: '/chat', type: 'primary' },
  { label: '简历优化', desc: '结构化评分与建议', path: '/resume/upload', type: 'success' },
  { label: '岗位分析', desc: 'JD 技能匹配', path: '/jobs', type: 'warning' },
  { label: '知识库问答', desc: '政策与流程检索', path: '/knowledge', type: 'info' }
]

onMounted(async () => {
  loading.value = true
  try {
    stats.value = await adminApi.stats()
  } catch (error) {
    console.error('Failed to load stats:', error)
  }
  try {
    recentSessions.value = (await chatApi.sessions()).slice(0, 6)
  } catch { /* ignore */ }
  loading.value = false
})
</script>

<template>
  <div class="page-grid">
    <div class="card-grid">
      <div v-for="card in statCards" :key="card.key" class="panel">
        <div class="muted">{{ card.label }}</div>
        <div class="mt-3 flex items-end justify-between">
          <span v-if="loading" class="metric-value">-</span>
          <span v-else class="metric-value">{{ stats?.[card.key as keyof AdminStats] || 0 }}</span>
          <el-tag :type="card.type">实时</el-tag>
        </div>
      </div>
    </div>

    <div class="grid gap-4 xl:grid-cols-[1.25fr_0.75fr]">
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
            <strong class="cursor-pointer" @click="$router.push('/chat')">
              {{ session.title || `会话 ${session.id}` }}
            </strong>
            <p class="muted mt-1">{{ session.sessionType || 'chat' }}</p>
          </el-timeline-item>
        </el-timeline>
      </section>

      <section class="panel">
        <div class="panel-title">快捷入口</div>
        <div v-for="entry in quickEntries" :key="entry.path" class="quick-entry" @click="$router.push(entry.path)">
          <div>
            <div class="font-semibold text-slate-50">{{ entry.label }}</div>
            <div class="muted text-sm">{{ entry.desc }}</div>
          </div>
          <el-tag :type="entry.type">进入</el-tag>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.quick-entry {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  margin-bottom: 10px;
  border-radius: 10px;
  background: #0f172a;
  cursor: pointer;
  transition: background .15s;
}
.quick-entry:hover { background: #1e293b; }
.quick-entry:last-child { margin-bottom: 0; }
</style>
