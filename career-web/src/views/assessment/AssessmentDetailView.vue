<script setup lang="ts">
import { computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAssessmentStore } from '@/stores/assessment'

const route = useRoute()
const router = useRouter()
const store = useAssessmentStore()
const assessmentId = computed(() => Number(route.params.id))
const question = computed(() => store.questions[store.currentIndex])
const questionNumber = computed(() => store.currentIndex + 1)
const completion = computed(() => {
  if (store.questions.length === 0) {
    return 0
  }
  return Math.round((questionNumber.value / store.questions.length) * 100)
})

watch(assessmentId, (id) => store.load(id), { immediate: true })

function next() {
  if (store.next()) {
    return
  }
  router.push(`/assessment/${route.params.id}/result`)
}

function selectAnswer(value: string | number | boolean | undefined) {
  if (question.value && typeof value !== 'undefined') {
    store.answer(question.value.id, Number(value))
  }
}
</script>

<template>
  <section class="panel mx-auto max-w-3xl">
    <div class="panel-title">
      <span>第 {{ questionNumber }} 题 / {{ store.questions.length }}</span>
      <el-tag>{{ completion }}%</el-tag>
    </div>
    <el-progress :percentage="completion" :stroke-width="12" />

    <div v-if="question" class="mt-8">
      <h2 class="text-2xl font-semibold text-slate-50">{{ question.text }}</h2>
      <el-radio-group class="mt-6 grid w-full gap-3" :model-value="store.answers[question.id]" @change="selectAnswer">
        <el-radio-button v-for="option in question.options" :key="option.value" :value="option.value">
          {{ option.label }}
        </el-radio-button>
      </el-radio-group>
    </div>

    <div class="mt-8 flex justify-between">
      <el-button :disabled="store.currentIndex === 0" @click="store.prev()">上一题</el-button>
      <el-button type="primary" :disabled="question && !store.answers[question.id]" @click="next">
        {{ store.currentIndex === store.questions.length - 1 ? '查看结果' : '下一题' }}
      </el-button>
    </div>
  </section>
</template>
