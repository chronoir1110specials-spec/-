<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { ModelConfig, ModelTestResult } from '@/types'
import { adminApi } from '@/api/admin'

const configs = ref<ModelConfig[]>([])
const loading = ref(false)
const testing = ref('')
const testResults = ref<Record<string, ModelTestResult>>({})
const dailyLimit = ref<number>(0)
const savingLimit = ref(false)

async function load() {
  configs.value = await adminApi.models()
  try {
    const rl = await adminApi.getRateLimit()
    dailyLimit.value = rl?.dailyLimit ?? 0
  } catch { /* ignore */ }
}

async function saveRateLimit() {
  savingLimit.value = true
  try {
    await adminApi.setRateLimit(Number(dailyLimit.value))
    ElMessage.success('调用限额已更新')
  } finally {
    savingLimit.value = false
  }
}

async function save(row: ModelConfig) {
  loading.value = true
  try {
    await adminApi.saveModel(row)
    ElMessage.success('模型配置已保存')
    await load()
  } finally {
    loading.value = false
  }
}

async function test(role: string) {
  testing.value = role
  try {
    const result = role === 'primary'
      ? await adminApi.testPrimaryModel()
      : await adminApi.testFallbackModel()
    testResults.value[role] = result
    if (result.reachable) {
      ElMessage.success(`${role} 连通正常 (${result.costTime ?? '-'}ms)`)
    } else {
      ElMessage.error(`${role} 连通失败：${result.errorMessage ?? '未知错误'}`)
    }
  } finally {
    testing.value = ''
  }
}

onMounted(load)
</script>

<template>
  <section class="panel">
    <div class="panel-title">模型配置</div>
    <el-table :data="configs" stripe>
      <el-table-column prop="modelRole" label="角色" width="110" />
      <el-table-column label="提供商" width="150"><template #default="{ row }"><el-input v-model="row.provider" /></template></el-table-column>
      <el-table-column label="模型" min-width="180"><template #default="{ row }"><el-input v-model="row.modelName" /></template></el-table-column>
      <el-table-column label="Base URL" min-width="240"><template #default="{ row }"><el-input v-model="row.baseUrl" /></template></el-table-column>
      <el-table-column label="API Key" min-width="180"><template #default="{ row }"><el-input v-model="row.apiKey" show-password placeholder="留空不覆盖" /></template></el-table-column>
      <el-table-column label="启用" width="90"><template #default="{ row }"><el-switch v-model="row.enabled" :active-value="1" :inactive-value="0" /></template></el-table-column>
      <el-table-column label="操作" width="190">
        <template #default="{ row }">
          <el-button link type="primary" :loading="loading" @click="save(row)">保存</el-button>
          <el-button
            v-if="row.modelRole === 'primary' || row.modelRole === 'fallback'"
            link
            type="success"
            :loading="testing === row.modelRole"
            @click="test(row.modelRole)"
          >测试连接</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div v-if="testResults.primary || testResults.fallback" class="test-results">
      <el-alert
        v-for="(res, role) in testResults"
        :key="role"
        :type="res.reachable ? 'success' : 'error'"
        :closable="false"
        show-icon
        class="test-result-item"
      >
        <template #title>
          <span>{{ role }}：{{ res.reachable ? '连通正常' : '连通失败' }}</span>
          <span v-if="res.modelName"> · {{ res.provider }}/{{ res.modelName }}</span>
          <span v-if="res.costTime != null"> · {{ res.costTime }}ms</span>
          <span v-if="res.totalTokens != null"> · {{ res.totalTokens }} tokens</span>
        </template>
        <div v-if="res.reachable && res.reply">回复：{{ res.reply }}</div>
        <div v-else-if="res.errorMessage">错误：{{ res.errorMessage }}</div>
      </el-alert>
    </div>

    <div class="rate-limit">
      <div class="panel-title">调用限额</div>
      <div class="rate-limit-row">
        <span>每用户每日调用上限</span>
        <el-input-number v-model="dailyLimit" :min="0" :step="10" />
        <el-button type="primary" :loading="savingLimit" @click="saveRateLimit">保存</el-button>
        <span class="hint">0 表示不限流</span>
      </div>
    </div>
  </section>
</template>

<style scoped>
.test-results {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.test-result-item :deep(.el-alert__title) {
  font-size: 13px;
}
.rate-limit { margin-top: 24px; }
.rate-limit-row { display: flex; align-items: center; gap: 12px; }
.rate-limit-row .hint { color: #94a3b8; font-size: 12px; }
</style>
