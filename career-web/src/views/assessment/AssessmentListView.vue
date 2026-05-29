<script setup lang="ts">
import { ref } from 'vue'
import { useAssessmentStore } from '@/stores/assessment'

const store = useAssessmentStore()
const currentPage = ref(1)
</script>

<template>
  <div class="page-grid">
    <section class="panel">
      <div class="panel-title">
        <span>职业测评</span>
        <el-input class="max-w-xs" placeholder="搜索测评" clearable />
      </div>
      <div class="card-grid">
        <el-card v-for="item in store.list" :key="item.id" shadow="never">
          <div class="flex items-start justify-between gap-3">
            <div>
              <h3 class="text-lg font-semibold text-slate-50">{{ item.name }}</h3>
              <p class="muted mt-2">{{ item.description }}</p>
            </div>
            <el-tag>{{ item.category }}</el-tag>
          </div>
          <div class="mt-4 flex flex-wrap gap-2">
            <el-tag v-for="tag in item.tags" :key="tag" type="info" size="small">{{ tag }}</el-tag>
          </div>
          <div class="mt-5 flex items-center justify-between muted">
            <span>{{ item.duration }} 分钟 · {{ item.questionCount }} 题</span>
            <span>{{ item.completedCount }} 人完成</span>
          </div>
          <el-button class="mt-5 w-full" type="primary" @click="$router.push(`/assessment/${item.id}`)">开始测评</el-button>
        </el-card>
      </div>
      <div class="mt-6 flex justify-end">
        <el-pagination v-model:current-page="currentPage" layout="prev, pager, next" :page-size="8" :total="32" />
      </div>
    </section>
  </div>
</template>
