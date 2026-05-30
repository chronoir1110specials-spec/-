<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Microphone, VideoCamera, SwitchButton } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useInterviewStore } from '@/stores/interview'
import { interviewApi } from '@/api/interview'

const router = useRouter()
const store = useInterviewStore()
const sessionId = ref<number | null>(null)
const questions = ref<string[]>([])
const activeQuestion = ref(0)
const answer = ref('')
const aiReply = ref('')
const loading = ref(false)
let timer: number | undefined

const remainingText = computed(() => {
  const minutes = Math.floor(store.remainingSeconds / 60).toString().padStart(2, '0')
  const seconds = (store.remainingSeconds % 60).toString().padStart(2, '0')
  return `${minutes}:${seconds}`
})

async function startInterview() {
  loading.value = true
  try {
    const data = await interviewApi.start('前端工程师', 'normal', 5)
    sessionId.value = data.sessionId
    questions.value = [data.question]
    activeQuestion.value = 0
  } catch (e) {
    ElMessage.error((e as Error).message)
  } finally {
    loading.value = false
  }
}

async function submitAnswerAndNext() {
  if (!sessionId.value) return
  if (!answer.value.trim()) {
    ElMessage.warning('请先输入回答')
    return
  }
  loading.value = true
  try {
    const reply = await interviewApi.answer(sessionId.value, answer.value)
    aiReply.value = reply?.content || ''
    answer.value = ''

    if (activeQuestion.value + 1 >= 5) {
      try {
        const s = await interviewApi.summary(sessionId.value)
        store.summaryText = s?.summary || ''
      } catch { /* 总结失败不阻塞 */ }
      ElMessage.success('面试已完成，跳转到反馈页')
      router.push('/interview/feedback')
      return
    }
    const next = await interviewApi.next(sessionId.value)
    questions.value.push(next.question)
    activeQuestion.value += 1
  } catch (e) {
    ElMessage.error((e as Error).message)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  timer = window.setInterval(store.tick, 1000)
  startInterview()
})

onBeforeUnmount(() => {
  if (timer) window.clearInterval(timer)
})
</script>

<template>
  <div class="grid gap-4 xl:grid-cols-[1fr_360px]">
    <section class="panel min-h-[620px]">
      <div class="panel-title">
        <span>模拟面试房间</span>
        <el-tag type="warning">倒计时 {{ remainingText }}</el-tag>
      </div>
      <div class="flex h-[500px] flex-col rounded-lg border border-gray-200 bg-gray-50">
        <div class="flex-1 space-y-5 overflow-auto p-5">
          <div v-if="questions.length" class="max-w-[76%] rounded-lg bg-white border border-gray-200 p-4">
            <strong>AI 面试官</strong>
            <p class="mt-2 whitespace-pre-wrap text-gray-700">{{ questions[activeQuestion] }}</p>
          </div>
          <div v-if="aiReply" class="max-w-[76%] rounded-lg bg-white border border-gray-200 p-4">
            <strong>点评</strong>
            <p class="mt-2 whitespace-pre-wrap text-gray-700">{{ aiReply }}</p>
          </div>
        </div>
        <div class="flex items-center gap-3 border-t border-gray-200 p-4">
          <el-input v-model="answer" type="textarea" :rows="2" placeholder="输入你的回答..." class="flex-1" />
          <el-button circle :icon="Microphone" type="primary" />
          <el-button circle :icon="VideoCamera" />
          <el-button circle :icon="SwitchButton" type="danger" @click="$router.push('/interview/feedback')" />
          <el-button type="primary" :loading="loading" @click="submitAnswerAndNext">提交并下一题</el-button>
        </div>
      </div>
    </section>

    <aside class="panel">
      <div class="panel-title">题目列表</div>
      <el-steps direction="vertical" :active="activeQuestion" finish-status="success">
        <el-step v-for="(q, i) in questions" :key="i" :title="q" />
      </el-steps>
    </aside>
  </div>
</template>
