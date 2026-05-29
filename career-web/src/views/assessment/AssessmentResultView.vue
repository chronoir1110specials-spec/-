<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { getAssessmentResult } from '@/mock'

const route = useRoute()
const assessmentResult = computed(() => getAssessmentResult(Number(route.params.id)))
</script>

<template>
  <div class="page-grid">
    <section class="panel">
      <div class="panel-title">测评结果：{{ assessmentResult.title }}</div>
      <p class="muted max-w-3xl">{{ assessmentResult.summary }}</p>
      <div class="mt-6 grid gap-4 md:grid-cols-2">
        <div v-for="score in assessmentResult.scores" :key="score.name">
          <div class="mb-2 flex justify-between">
            <span>{{ score.name }}</span>
            <span>{{ score.score }}</span>
          </div>
          <el-progress :percentage="score.score" />
        </div>
      </div>
    </section>

    <section class="panel">
      <div class="panel-title">推荐职业</div>
      <div class="card-grid">
        <el-card v-for="career in assessmentResult.careers" :key="career.title" shadow="never">
          <div class="flex justify-between">
            <h3 class="font-semibold text-slate-50">{{ career.title }}</h3>
            <el-tag type="success">匹配 {{ career.match }}%</el-tag>
          </div>
          <p class="muted mt-3">{{ career.reason }}</p>
        </el-card>
      </div>
    </section>
  </div>
</template>
