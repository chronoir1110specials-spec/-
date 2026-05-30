<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { ChatMessage, ChatSession } from '@/types'
import { chatApi } from '@/api/chat'
import MarkdownRenderer from '@/components/MarkdownRenderer.vue'

const sessions = ref<ChatSession[]>([])
const messages = ref<ChatMessage[]>([])
const currentSessionId = ref<number>()
const input = ref('')
const loading = ref(false)
const messageBox = ref<HTMLElement>()

async function loadSessions() {
  sessions.value = await chatApi.sessions()
  if (!currentSessionId.value && sessions.value.length) await selectSession(sessions.value[0].id)
}

async function createSession() {
  const session = await chatApi.createSession('新的对话')
  sessions.value.unshift(session)
  await selectSession(session.id)
}

async function selectSession(id: number) {
  currentSessionId.value = id
  messages.value = await chatApi.messages(id)
  await scrollBottom()
}

async function send() {
  const content = input.value.trim()
  if (!content) return
  if (!currentSessionId.value) await createSession()
  if (!currentSessionId.value) return
  const sessionId = currentSessionId.value
  input.value = ''
  messages.value.push({ id: Date.now(), sessionId, role: 'user', content })
  loading.value = true

  // 占位的助手消息，流式增量写入这里
  const assistantId = Date.now() + 1
  const assistant: ChatMessage = { id: assistantId, sessionId, role: 'assistant', content: '' }
  messages.value.push(assistant)
  await scrollBottom()

  let gotContent = false
  try {
    await chatApi.stream(sessionId, content, {
      onDelta: (piece) => {
        gotContent = true
        assistant.content += piece
        void scrollBottom()
      },
      onDone: (meta) => {
        if (meta?.modelName) assistant.modelName = meta.modelName
      },
      onError: (msg) => {
        ElMessage.error(msg || '发送失败')
        if (!gotContent) {
          // 移除空的占位助手消息
          messages.value = messages.value.filter((m) => m.id !== assistantId)
        }
      }
    })
    await loadSessions()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '发送失败')
    if (!gotContent) messages.value = messages.value.filter((m) => m.id !== assistantId)
  } finally {
    loading.value = false
    await scrollBottom()
  }
}

async function scrollBottom() {
  await nextTick()
  if (messageBox.value) messageBox.value.scrollTop = messageBox.value.scrollHeight
}

onMounted(loadSessions)
</script>

<template>
  <div class="chat-grid">
    <section class="panel session-panel">
      <div class="panel-title">
        <span>历史会话</span>
        <el-button size="small" type="primary" @click="createSession">新建</el-button>
      </div>
      <el-menu class="session-menu" :default-active="String(currentSessionId || '')" @select="(key: string) => selectSession(Number(key))">
        <el-menu-item v-for="session in sessions" :key="session.id" :index="String(session.id)">
          {{ session.title || `会话 ${session.id}` }}
        </el-menu-item>
      </el-menu>
    </section>

    <section class="panel chat-panel">
      <div ref="messageBox" class="messages">
        <el-empty v-if="messages.length === 0" description="开始一次就业辅导对话" />
        <div v-for="message in messages" :key="message.id" class="message" :class="message.role">
          <div class="bubble">
            <div class="role">{{ message.role === 'user' ? '我' : 'Career Agent' }}</div>
            <div v-if="message.role === 'user'" class="bubble-text">{{ message.content }}</div>
            <MarkdownRenderer v-else class="bubble-text" :content="message.content" />
          </div>
        </div>
      </div>
      <div class="sender">
        <el-input v-model="input" type="textarea" :rows="3" resize="none" placeholder="输入你的求职、简历或面试问题" @keydown.enter.exact.prevent="send" />
        <el-button type="primary" :loading="loading" @click="send">发送</el-button>
      </div>
    </section>
  </div>
</template>

<style scoped>
.chat-grid {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 16px;
  height: calc(100vh - 112px);
}
.session-panel { display: flex; flex-direction: column; overflow: hidden; }
.session-menu { flex: 1; min-height: 0; overflow: auto; border-right: 0; }
.chat-panel { display: flex; flex-direction: column; overflow: hidden; }
.messages { flex: 1; min-height: 0; overflow-y: auto; padding-right: 8px; }
.message { display: flex; margin-bottom: 16px; }
.message.user { justify-content: flex-end; }
.bubble {
  max-width: 72%;
  border-radius: 14px;
  padding: 12px 14px;
  background: #f2f4f8;
  color: #1f2329;
  white-space: pre-wrap;
  word-break: break-word;
  overflow-wrap: anywhere;
  border: 1px solid #e9ecf2;
}
.message.user .bubble { background: #14b8a6; color: #fff; border-color: #14b8a6; }
.bubble-text { min-width: 0; max-width: 100%; overflow-wrap: anywhere; }
.role { font-size: 12px; opacity: .65; margin-bottom: 6px; }
.sender { display: grid; grid-template-columns: 1fr auto; gap: 12px; align-items: end; margin-top: 14px; flex-shrink: 0; }
@media (max-width: 1024px) {
  .chat-grid { grid-template-columns: 1fr; height: auto; }
  .chat-panel { height: calc(100vh - 220px); }
}
</style>
