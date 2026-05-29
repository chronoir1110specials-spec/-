<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import type { Job } from '@/types'
import { jobsApi } from '@/api/jobs'

const route = useRoute()
const job = ref<Job>()
const rawAnalysis = computed(() => job.value?.raw?.analysisResult || '')

onMounted(async () => {
  job.value = await jobsApi.detail(Number(route.params.id))
})
</script>

<template>
  <div v-if="job" class="page-grid">
    <section class="panel">
      <div class="flex flex-wrap items-start justify-between gap-4">
        <div>
          <div class="panel-title mb-2">{{ job.title }}</div>
          <p class="muted">{{ job.company }} · {{ job.city }} · {{ job.salary }}</p>
          <p class="mt-4 max-w-3xl whitespace-pre-wrap text-slate-300">{{ job.description }}</p>
        </div>
        <el-progress type="dashboard" :percentage="job.match" :width="130" />
      </div>
    </section>

    <section class="grid gap-4 lg:grid-cols-3">
      <div class="panel lg:col-span-2">
        <div class="panel-title">岗位要求</div>
        <el-empty v-if="job.requirements.length === 0" description="暂无结构化岗位要求" />
        <el-check-tag v-for="item in job.requirements" :key="item" class="mr-2 mb-2" checked>{{ item }}</el-check-tag>
        <div class="panel-title mt-8">模型分析原文</div>
        <pre class="whitespace-pre-wrap text-slate-300">{{ rawAnalysis || '暂无分析内容' }}</pre>
      </div>
      <div class="panel">
        <div class="panel-title">提升方向</div>
        <el-empty v-if="job.gaps.length === 0" description="暂无提升建议" />
        <el-alert v-for="gap in job.gaps" :key="gap" class="mb-3" :title="gap" type="warning" show-icon :closable="false" />
        <div class="panel-title mt-6">岗位亮点</div>
        <el-tag v-for="highlight in job.highlights" :key="highlight" class="mr-2 mb-2" type="success">{{ highlight }}</el-tag>
      </div>
    </section>
  </div>
</template>
