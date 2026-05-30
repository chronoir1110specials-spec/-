<script setup lang="ts">
import { onMounted, ref } from 'vue'
import type { AgentStepLog, AgentTask, ModelCallLog } from '@/types'
import { adminApi } from '@/api/admin'

const modelLogs = ref<ModelCallLog[]>([])
const agentLogs = ref<AgentTask[]>([])
const steps = ref<AgentStepLog[]>([])
const selectedTaskId = ref<number | null>(null)

async function load() {
  const [models, agents] = await Promise.all([adminApi.modelLogs(), adminApi.agentLogs()])
  modelLogs.value = models || []
  agentLogs.value = agents || []
}

async function loadSteps(row: AgentTask) {
  selectedTaskId.value = row.id
  steps.value = await adminApi.agentSteps(row.id)
}

const stepTypeTag = (t?: string) => {
  if (t === 'tool_call') return 'warning'
  if (t === 'model') return 'primary'
  if (t === 'retrieval' || t === 'embedding') return 'success'
  if (t === 'output_parse') return 'info'
  return 'info'
}
const stepTypeLabel = (t?: string) => {
  const map: Record<string, string> = {
    tool_call: '工具调用', model: '模型生成', retrieval: '检索', embedding: '向量化',
    context: '上下文', output_parse: '输出解析'
  }
  return (t && map[t]) || t || '-'
}

onMounted(load)
</script>

<template>
  <div class="page-grid">
    <section class="panel">
      <div class="panel-title">Agent 任务日志 <span class="muted">（点击行查看工具调用链）</span></div>
      <el-table :data="agentLogs" stripe @row-click="loadSteps" highlight-current-row>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="agentType" label="Agent" width="150" />
        <el-table-column prop="taskType" label="任务" width="140" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'succeeded' ? 'success' : (row.status === 'failed' ? 'danger' : 'info')" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="inputSummary" label="输入摘要" min-width="220" show-overflow-tooltip />
        <el-table-column prop="totalTokens" label="Tokens" width="90" />
        <el-table-column prop="totalCostTime" label="耗时ms" width="90" />
      </el-table>
    </section>

    <section class="panel">
      <div class="panel-title">
        <span>步骤日志（工具链 / 检索 / 生成）</span>
        <span v-if="selectedTaskId" class="muted">任务 #{{ selectedTaskId }}</span>
      </div>
      <el-empty v-if="steps.length === 0" description="点击上方任务查看其执行步骤" />
      <el-table v-else :data="steps" stripe>
        <el-table-column prop="stepIndex" label="#" width="60" />
        <el-table-column label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="stepTypeTag(row.stepType)" size="small">{{ stepTypeLabel(row.stepType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="stepName" label="步骤" width="180" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'succeeded' ? 'success' : 'danger'" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="outputSummary" label="输出摘要" min-width="240" show-overflow-tooltip />
        <el-table-column prop="errorMessage" label="错误" min-width="160" show-overflow-tooltip />
      </el-table>
    </section>

    <section class="panel">
      <div class="panel-title">模型调用日志</div>
      <el-table :data="modelLogs" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="modelName" label="模型" min-width="170" />
        <el-table-column prop="provider" label="提供商" width="120" />
        <el-table-column label="链路" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isFallback ? 'warning' : 'success'" size="small">{{ row.isFallback ? '兜底' : '主模型' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalTokens" label="Tokens" width="90" />
        <el-table-column prop="costTime" label="耗时ms" width="90" />
        <el-table-column label="结果" width="90">
          <template #default="{ row }">
            <el-tag :type="row.success ? 'success' : 'danger'" size="small">{{ row.success ? '成功' : '失败' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="errorMessage" label="错误" min-width="180" show-overflow-tooltip />
      </el-table>
    </section>
  </div>
</template>
