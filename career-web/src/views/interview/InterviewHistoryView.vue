<script setup lang="ts">
import { useInterviewStore } from '@/stores/interview'
import type { InterviewSession } from '@/types'

const store = useInterviewStore()

function feedbackPath(row: InterviewSession) {
  return { path: '/interview/feedback', query: { id: row.id } }
}
</script>

<template>
  <section class="panel">
    <div class="panel-title">
      <span>面试历史</span>
      <el-button type="primary" @click="$router.push('/interview/room')">开始训练</el-button>
    </div>
    <el-table :data="store.history" stripe>
      <el-table-column prop="role" label="岗位" min-width="160" />
      <el-table-column prop="company" label="公司" min-width="140" />
      <el-table-column prop="time" label="时间" width="180" />
      <el-table-column prop="score" label="得分" width="100" />
      <el-table-column prop="status" label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="row.status === '进行中' ? 'warning' : 'success'">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button link type="primary" @click="$router.push(feedbackPath(row))">查看反馈</el-button>
          <el-button link @click="$router.push(feedbackPath(row))">复盘</el-button>
        </template>
      </el-table-column>
    </el-table>
  </section>
</template>
