<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useResumeStore } from '@/stores/resume'
import { resumeApi } from '@/api/resume'

const route = useRoute()
const store = useResumeStore()
const optimizing = ref(false)
const id = computed(() => Number(route.query.id || 0) || undefined)

async function optimizeLatest() {
  const resume = id.value ? await resumeApi.detail(id.value) : await resumeApi.latest()
  if (!resume?.content) {
    ElMessage.warning('当前简历没有可优化的文本内容')
    return
  }
  optimizing.value = true
  try {
    store.analysis = await resumeApi.optimize(resume.content, resume.resumeName || resume.originalFileName)
    ElMessage.success('优化分析已生成')
  } finally {
    optimizing.value = false
  }
}

onMounted(() => store.loadAnalysis(id.value))
</script>

<template>
  <div class="page-grid">
    <section class="panel">
      <div class="panel-title">
        <span>综合评分</span>
        <el-button type="primary" :loading="optimizing" @click="optimizeLatest">重新优化</el-button>
      </div>
      <div class="flex flex-wrap items-center gap-8">
        <el-progress type="dashboard" :percentage="store.analysis.score" :width="150" />
        <div>
          <h2 class="text-2xl font-semibold text-slate-800">{{ store.analysis.summary || '简历分析结果' }}</h2>
          <p class="muted mt-2">评分和建议来自当前简历解析结果或模型优化输出。</p>
        </div>
      </div>
    </section>

    <section v-if="store.analysis.dimensions.length" class="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
      <el-card v-for="item in store.analysis.dimensions" :key="item.name" shadow="never">
        <div class="flex items-center justify-between">
          <strong>{{ item.name }}</strong>
          <el-tag>{{ item.score }}</el-tag>
        </div>
        <p class="muted mt-3">{{ item.comment }}</p>
      </el-card>
    </section>

    <section class="grid gap-4 lg:grid-cols-2">
      <div class="panel">
        <div class="panel-title">问题列表</div>
        <el-empty v-if="!store.analysis.issues.length" description="暂无问题列表" />
        <el-alert v-for="issue in store.analysis.issues" :key="issue" class="mb-3" :title="issue" type="warning" show-icon :closable="false" />
      </div>
      <div class="panel">
        <div class="panel-title">优化建议</div>
        <el-empty v-if="!store.analysis.suggestions.length" description="暂无优化建议" />
        <el-alert v-for="suggestion in store.analysis.suggestions" :key="suggestion" class="mb-3" :title="suggestion" type="success" show-icon :closable="false" />
      </div>
    </section>
  </div>
</template>
