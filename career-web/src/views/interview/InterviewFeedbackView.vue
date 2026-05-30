<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useInterviewStore } from '@/stores/interview'
import MarkdownRenderer from '@/components/MarkdownRenderer.vue'

const router = useRouter()
const store = useInterviewStore()
</script>

<template>
  <div class="page-grid">
    <section class="panel">
      <div class="panel-title">
        <span>面试总结</span>
        <el-button type="primary" link @click="router.push('/interview/room')">再来一次</el-button>
      </div>
      <el-empty v-if="!store.summaryText" description="还没有面试总结，先去完成一次模拟面试" />
      <MarkdownRenderer v-else :content="store.summaryText" />
    </section>
  </div>
</template>
