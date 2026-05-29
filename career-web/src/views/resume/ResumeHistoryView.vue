<script setup lang="ts">
import { onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useResumeStore } from '@/stores/resume'
import { resumeApi } from '@/api/resume'
import type { ResumeRecord } from '@/types'

const store = useResumeStore()

function analysisPath(row: ResumeRecord) {
  return { path: '/resume/analysis', query: { id: row.id } }
}

async function remove(row: ResumeRecord) {
  await ElMessageBox.confirm(`确认删除 ${row.fileName}？`, '删除简历', { type: 'warning' })
  await resumeApi.delete(row.id)
  ElMessage.success('已删除')
  await store.loadHistory()
}

onMounted(() => store.loadHistory())
</script>

<template>
  <section class="panel">
    <div class="panel-title">
      <span>历史简历</span>
      <el-button type="primary" @click="$router.push('/resume/upload')">上传新简历</el-button>
    </div>
    <el-table :data="store.history" stripe>
      <el-table-column prop="version" label="版本" width="90" />
      <el-table-column prop="fileName" label="文件名" min-width="220" />
      <el-table-column prop="uploadTime" label="上传时间" width="180" />
      <el-table-column prop="score" label="评分" width="100" />
      <el-table-column prop="status" label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="row.status === '解析失败' ? 'danger' : row.status === '需优化' ? 'warning' : 'success'">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button link type="primary" @click="$router.push(analysisPath(row))">查看分析</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </section>
</template>
