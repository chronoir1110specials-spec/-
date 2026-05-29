<script setup lang="ts">
import { useInterviewStore } from '@/stores/interview'

const store = useInterviewStore()
</script>

<template>
  <div class="page-grid">
    <section class="panel">
      <div class="panel-title">面试反馈</div>
      <div class="flex flex-wrap items-center gap-8">
        <el-progress type="dashboard" :percentage="store.feedback.overall" :width="150" />
        <div class="grid flex-1 gap-4 md:grid-cols-2">
          <div v-for="dimension in store.feedback.dimensions" :key="dimension.name">
            <div class="mb-2 flex justify-between">
              <span>{{ dimension.name }}</span>
              <span>{{ dimension.score }}</span>
            </div>
            <el-progress :percentage="dimension.score" />
          </div>
        </div>
      </div>
    </section>

    <section class="panel">
      <div class="panel-title">逐题点评</div>
      <el-collapse>
        <el-collapse-item v-for="item in store.feedback.questions" :key="item.id" :title="item.question" :name="item.id">
          <p><strong>回答摘要：</strong>{{ item.answer }}</p>
          <p class="mt-2"><strong>点评：</strong>{{ item.feedback }}</p>
          <el-tag class="mt-3">得分 {{ item.score }}</el-tag>
        </el-collapse-item>
      </el-collapse>
    </section>

    <section class="panel">
      <div class="panel-title">改进建议</div>
      <el-alert v-for="suggestion in store.feedback.suggestions" :key="suggestion" class="mb-3" :title="suggestion" type="success" show-icon :closable="false" />
    </section>
  </div>
</template>
