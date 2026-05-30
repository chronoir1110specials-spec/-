<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { UploadFilled } from '@element-plus/icons-vue'
import { ElMessage, type UploadUserFile } from 'element-plus'
import { resumeApi } from '@/api/resume'
import { useResumeStore } from '@/stores/resume'
import type { ResumeRecord } from '@/types'

const store = useResumeStore()
const uploading = ref(false)
const optimizing = ref(false)
const fileList = ref<UploadUserFile[]>([])
const activeId = ref<number | undefined>(undefined)

const scoreColor = computed(() => {
  const s = store.analysis.score
  if (s >= 85) return '#14b8a6'
  if (s >= 70) return '#f59e0b'
  return '#ef4444'
})

const scoreLabel = computed(() => {
  const s = store.analysis.score
  if (s >= 85) return '优秀'
  if (s >= 70) return '良好'
  if (s > 0) return '需优化'
  return '待评分'
})

async function refresh() {
  await store.loadHistory()
}

async function uploadResume() {
  const file = fileList.value[0]?.raw
  if (!file || uploading.value) return
  uploading.value = true
  store.setUploadProgress(0)
  try {
    const resume = await resumeApi.upload(file, (p) => store.setUploadProgress(p))
    fileList.value = []
    await refresh()
    activeId.value = resume.id
    await store.loadAnalysis(resume.id)
    ElMessage.success(resume.parseStatus === 'failed' ? '上传成功，但解析失败' : '上传并解析成功')
  } finally {
    uploading.value = false
  }
}

async function selectResume(row: ResumeRecord) {
  activeId.value = row.id
  await store.loadAnalysis(row.id)
}

async function optimize() {
  const resume = activeId.value ? await resumeApi.detail(activeId.value) : await resumeApi.latest()
  if (!resume?.content) {
    ElMessage.warning('当前简历没有可优化的文本内容')
    return
  }
  optimizing.value = true
  try {
    store.analysis = await resumeApi.optimize(resume.content, resume.resumeName || resume.originalFileName)
    ElMessage.success('AI 优化分析已生成')
  } finally {
    optimizing.value = false
  }
}

async function removeResume(row: ResumeRecord) {
  await resumeApi.delete(row.id)
  ElMessage.success('已删除')
  if (activeId.value === row.id) {
    activeId.value = undefined
    store.analysis = { score: 0, dimensions: [], issues: [], suggestions: [] }
  }
  await refresh()
}

onMounted(async () => {
  await refresh()
  const latest = store.history[0]
  if (latest) {
    activeId.value = latest.id
    await store.loadAnalysis(latest.id)
  }
})
</script>

<template>
  <div class="workbench">
    <!-- 左栏：上传 + 简历列表 -->
    <aside class="wb-left">
      <section class="panel">
        <div class="panel-title">上传简历</div>
        <el-upload
          v-model:file-list="fileList"
          drag
          action="#"
          :auto-upload="false"
          :limit="1"
          accept=".pdf,.docx,.txt"
        >
          <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
          <div class="el-upload__text">拖拽或点击选择</div>
          <template #tip><div class="el-upload__tip">PDF / DOCX / TXT，&lt; 10MB</div></template>
        </el-upload>
        <el-button
          type="primary"
          class="mt-3 w-full"
          :disabled="fileList.length === 0 || uploading"
          :loading="uploading"
          @click="uploadResume"
        >上传并解析</el-button>
        <el-progress v-if="uploading" class="mt-3" :percentage="store.uploadProgress" :stroke-width="8" />
      </section>

      <section class="panel">
        <div class="panel-title">我的简历</div>
        <el-empty v-if="store.history.length === 0" description="还没有简历" :image-size="80" />
        <ul v-else class="resume-list">
          <li
            v-for="item in store.history"
            :key="item.id"
            class="resume-item"
            :class="{ active: item.id === activeId }"
            @click="selectResume(item)"
          >
            <div class="resume-item-main">
              <span class="resume-name">{{ item.fileName }}</span>
              <span class="resume-meta">{{ item.version }} · {{ item.status }}</span>
            </div>
            <div class="resume-item-right">
              <span class="resume-score" v-if="item.score">{{ item.score }}</span>
              <el-button link type="danger" size="small" @click.stop="removeResume(item)">删除</el-button>
            </div>
          </li>
        </ul>
      </section>
    </aside>

    <!-- 右栏：评分总览 + 维度 + 问题/建议 -->
    <main class="wb-right">
      <section class="panel score-panel">
        <div class="panel-title">
          <span>简历评分总览</span>
          <el-button type="primary" :loading="optimizing" @click="optimize">AI 优化分析</el-button>
        </div>
        <div class="score-body">
          <el-progress type="dashboard" :percentage="store.analysis.score" :color="scoreColor" :width="160">
            <template #default>
              <div class="score-inner">
                <div class="score-num" :style="{ color: scoreColor }">{{ store.analysis.score }}</div>
                <div class="score-tag">{{ scoreLabel }}</div>
              </div>
            </template>
          </el-progress>
          <div class="score-summary">
            <h2>{{ store.analysis.summary || '上传简历后查看 AI 评分与优化建议' }}</h2>
            <div v-if="store.analysis.keywords?.length" class="mt-3">
              <el-tag v-for="k in store.analysis.keywords" :key="k" class="kw-tag" effect="light">{{ k }}</el-tag>
            </div>
          </div>
        </div>
      </section>

      <section v-if="store.analysis.dimensions.length" class="dim-grid">
        <div v-for="item in store.analysis.dimensions" :key="item.name" class="panel dim-card">
          <div class="dim-head">
            <strong>{{ item.name }}</strong>
            <span class="dim-score">{{ item.score }}</span>
          </div>
          <p class="muted dim-comment">{{ item.comment }}</p>
        </div>
      </section>

      <div class="issue-grid">
        <section class="panel">
          <div class="panel-title">问题列表</div>
          <el-empty v-if="!store.analysis.issues.length" description="暂无问题" :image-size="70" />
          <el-alert
            v-for="issue in store.analysis.issues"
            :key="issue"
            class="mb-2"
            :title="issue"
            type="warning"
            :closable="false"
            show-icon
          />
        </section>
        <section class="panel">
          <div class="panel-title">优化建议</div>
          <el-empty v-if="!store.analysis.suggestions.length" description="暂无建议" :image-size="70" />
          <el-alert
            v-for="s in store.analysis.suggestions"
            :key="s"
            class="mb-2"
            :title="s"
            type="success"
            :closable="false"
            show-icon
          />
        </section>
      </div>
    </main>
  </div>
</template>

<style scoped>
.workbench { display: grid; grid-template-columns: 320px 1fr; gap: 18px; align-items: start; }
.wb-left { display: flex; flex-direction: column; gap: 18px; position: sticky; top: 84px; }
.wb-right { display: flex; flex-direction: column; gap: 18px; }

.resume-list { list-style: none; display: flex; flex-direction: column; gap: 8px; }
.resume-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: 10px 12px; border: 1px solid var(--border-color); border-radius: 10px;
  cursor: pointer; transition: all .15s ease;
}
.resume-item:hover { border-color: #2dd4bf; background: #f0fdfa; }
.resume-item.active { border-color: #14b8a6; background: #ecfdf9; box-shadow: 0 0 0 1px #14b8a6 inset; }
.resume-item-main { display: flex; flex-direction: column; gap: 3px; overflow: hidden; }
.resume-name { font-weight: 600; font-size: 14px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 150px; }
.resume-meta { font-size: 12px; color: var(--text-secondary); }
.resume-item-right { display: flex; align-items: center; gap: 8px; }
.resume-score { font-weight: 800; color: #0d9488; }

.score-body { display: flex; align-items: center; gap: 36px; flex-wrap: wrap; }
.score-inner { text-align: center; }
.score-num { font-size: 34px; font-weight: 800; line-height: 1; }
.score-tag { font-size: 13px; color: var(--text-secondary); margin-top: 4px; }
.score-summary h2 { font-size: 20px; font-weight: 700; color: var(--text-primary); line-height: 1.5; }
.kw-tag { margin: 0 6px 6px 0; }

.dim-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 14px; }
.dim-card { padding: 16px 18px; }
.dim-head { display: flex; align-items: center; justify-content: space-between; }
.dim-score { font-weight: 800; color: #0d9488; font-size: 18px; }
.dim-comment { margin-top: 8px; font-size: 13px; line-height: 1.6; }

.issue-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 18px; }

@media (max-width: 1024px) {
  .workbench { grid-template-columns: 1fr; }
  .wb-left { position: static; }
  .issue-grid { grid-template-columns: 1fr; }
}
</style>
