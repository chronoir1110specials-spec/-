<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { adminApi, type AgentCapabilities } from '@/api/admin'

const caps = ref<AgentCapabilities>({ tools: [], agents: [] })
const loading = ref(false)

const ragText: Record<string, string> = { none: '不检索', optional: '可选检索', required: '强制检索' }
const memText: Record<string, string> = { none: '无记忆', session: '会话记忆', profile: '画像记忆', history: '历史记忆' }
const modelText: Record<string, string> = { primary_only: '仅主模型', primary_with_fallback: '主+兜底' }

onMounted(async () => {
  loading.value = true
  try {
    caps.value = await adminApi.capabilities()
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="page-grid" v-loading="loading">
    <section class="panel">
      <div class="panel-title">已注册工具（ToolRegistry）</div>
      <el-table :data="caps.tools" stripe>
        <el-table-column prop="name" label="工具名" width="220" />
        <el-table-column prop="description" label="用途" min-width="280" show-overflow-tooltip />
        <el-table-column label="只读" width="90">
          <template #default="{ row }">
            <el-tag :type="row.readOnly ? 'success' : 'warning'" size="small">{{ row.readOnly ? '只读' : '写' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="风险" width="100">
          <template #default="{ row }">
            <el-tag :type="row.destructive ? 'danger' : 'info'" size="small">{{ row.destructive ? '破坏性' : '安全' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <section class="panel">
      <div class="panel-title">Agent 定义（能力策略）</div>
      <el-table :data="caps.agents" stripe>
        <el-table-column prop="displayName" label="Agent" width="170" />
        <el-table-column prop="agentType" label="类型" width="150" />
        <el-table-column label="工具白名单" min-width="240">
          <template #default="{ row }">
            <el-tag v-for="t in row.allowedTools" :key="t" size="small" class="tool-tag">{{ t }}</el-tag>
            <span v-if="!row.allowedTools?.length" class="muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="RAG" width="110"><template #default="{ row }">{{ ragText[row.ragPolicy] || row.ragPolicy }}</template></el-table-column>
        <el-table-column label="记忆" width="110"><template #default="{ row }">{{ memText[row.memoryPolicy] || row.memoryPolicy }}</template></el-table-column>
        <el-table-column label="模型" width="110"><template #default="{ row }">{{ modelText[row.modelPolicy] || row.modelPolicy }}</template></el-table-column>
        <el-table-column prop="maxToolCalls" label="工具上限" width="100" />
      </el-table>
    </section>
  </div>
</template>

<style scoped>
.tool-tag { margin: 2px 4px 2px 0; }
</style>
