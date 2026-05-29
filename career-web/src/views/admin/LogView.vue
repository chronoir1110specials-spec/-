<script setup lang="ts">
import { onMounted, ref } from 'vue'
import type { AgentStepLog, AgentTask, ModelCallLog } from '@/types'
import { adminApi } from '@/api/admin'

const modelLogs = ref<ModelCallLog[]>([])
const agentLogs = ref<AgentTask[]>([])
const steps = ref<AgentStepLog[]>([])

async function load() {
  const [models, agents] = await Promise.all([adminApi.modelLogs(), adminApi.agentLogs()])
  modelLogs.value = models || []
  agentLogs.value = agents || []
}

async function loadSteps(row: AgentTask) {
  steps.value = await adminApi.agentSteps(row.id)
}

onMounted(load)
</script>

<template>
  <div class="page-grid">
    <section class="panel">
      <div class="panel-title">Agent 任务日志</div>
      <el-table :data="agentLogs" stripe @row-click="loadSteps">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="agentType" label="Agent" width="150" />
        <el-table-column prop="taskType" label="任务" width="150" />
        <el-table-column prop="status" label="状态" width="110" />
        <el-table-column prop="inputSummary" label="输入摘要" min-width="220" show-overflow-tooltip />
        <el-table-column prop="totalTokens" label="Tokens" width="100" />
        <el-table-column prop="totalCostTime" label="耗时ms" width="100" />
      </el-table>
    </section>

    <section class="panel">
      <div class="panel-title">步骤日志</div>
      <el-table :data="steps" stripe>
        <el-table-column prop="stepIndex" label="#" width="70" />
        <el-table-column prop="stepType" label="类型" width="120" />
        <el-table-column prop="stepName" label="步骤" width="180" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column prop="outputSummary" label="输出摘要" min-width="240" show-overflow-tooltip />
        <el-table-column prop="errorMessage" label="错误" min-width="180" show-overflow-tooltip />
      </el-table>
    </section>

    <section class="panel">
      <div class="panel-title">模型调用日志</div>
      <el-table :data="modelLogs" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="modelRole" label="角色" width="120" />
        <el-table-column prop="modelName" label="模型" min-width="180" />
        <el-table-column prop="provider" label="提供商" width="120" />
        <el-table-column prop="totalTokens" label="Tokens" width="100" />
        <el-table-column prop="costTime" label="耗时ms" width="100" />
        <el-table-column prop="success" label="成功" width="90" />
        <el-table-column prop="errorMessage" label="错误" min-width="200" show-overflow-tooltip />
      </el-table>
    </section>
  </div>
</template>
