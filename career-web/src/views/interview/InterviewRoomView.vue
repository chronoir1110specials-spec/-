<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Microphone, VideoCamera, SwitchButton } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useInterviewStore } from '@/stores/interview'
import { interviewApi } from '@/api/interview'

interface EvaluationResult {
  score?: number
  comment?: string
  advantages?: string
  problems?: string
  referenceAnswer?: string
}

const router = useRouter()
const store = useInterviewStore()
const sessionId = ref<number | null>(null)
const questions = ref<string[]>([])
const activeQuestion = ref(0)
const answer = ref('')
const aiReply = ref<EvaluationResult | null>(null)
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

function parseEvaluation(content: string): EvaluationResult | null {
  if (!content) return null
  try {
    // 尝试清理 markdown 代码块标记
    const cleaned = content.replace(/```json\s*/gi, '').replace(/```/g, '').trim()
    return JSON.parse(cleaned) as EvaluationResult
  } catch {
    // 如果解析失败，返回原始文本作为 comment
    return { comment: content }
  }
}

function formatCodeInText(text: string): string {
  if (!text) return ''
  // 为代码片段添加代码框（简单识别：包含常见代码关键字或符号）
  return text.replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>')
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
    // 解析 JSON 格式的点评
    aiReply.value = parseEvaluation(reply?.content || '')
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

          <!-- AI 点评区域 - 格式化显示 -->
          <div v-if="aiReply" class="max-w-[76%] rounded-lg bg-white border border-gray-200 p-4">
            <div class="flex items-center justify-between mb-3">
              <strong class="text-lg">AI 点评</strong>
              <el-tag v-if="aiReply.score !== undefined" :type="aiReply.score >= 80 ? 'success' : aiReply.score >= 60 ? 'warning' : 'danger'" size="large">
                得分：{{ aiReply.score }}
              </el-tag>
            </div>

            <!-- 总体评价 -->
            <div v-if="aiReply.comment" class="mb-4">
              <div class="text-sm font-semibold text-gray-600 mb-1">📝 总体评价</div>
              <p class="text-gray-700 whitespace-pre-wrap" v-html="formatCodeInText(aiReply.comment)"></p>
            </div>

            <!-- 优点 -->
            <div v-if="aiReply.advantages" class="mb-4">
              <div class="text-sm font-semibold text-green-600 mb-1">✅ 优点</div>
              <p class="text-gray-700 whitespace-pre-wrap" v-html="formatCodeInText(aiReply.advantages)"></p>
            </div>

            <!-- 问题 -->
            <div v-if="aiReply.problems" class="mb-4">
              <div class="text-sm font-semibold text-red-600 mb-1">❌ 存在问题</div>
              <p class="text-gray-700 whitespace-pre-wrap" v-html="formatCodeInText(aiReply.problems)"></p>
            </div>

            <!-- 参考答案 -->
            <div v-if="aiReply.referenceAnswer" class="bg-blue-50 rounded p-3 border border-blue-200">
              <div class="text-sm font-semibold text-blue-700 mb-2">💡 参考答案</div>
              <p class="text-gray-700 whitespace-pre-wrap" v-html="formatCodeInText(aiReply.referenceAnswer)"></p>
            </div>
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

<style scoped>
:deep(.inline-code) {
  background-color: #f5f5f5;
  border: 1px solid #e0e0e0;
  border-radius: 3px;
  padding: 2px 6px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 0.9em;
  color: #d63384;
}

/* 修复题目列表被撑大的问题 */
aside.panel {
  height: fit-content;
  align-self: flex-start;
}

/* 确保 el-steps 不使用 space-between */
:deep(.el-steps) {
  display: flex;
  flex-direction: column;
  justify-content: flex-start !important;
  align-content: flex-start !important;
}

/* 防止单个 step 被拉伸 */
:deep(.el-step) {
  flex: none !important;
  height: auto !important;
}

/* 限制 step 内容的高度 */
:deep(.el-step__main) {
  flex: none !important;
}
</style>
