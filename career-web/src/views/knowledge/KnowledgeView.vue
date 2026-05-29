<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, type UploadUserFile } from 'element-plus'
import type { KbDocument } from '@/types'
import { knowledgeApi } from '@/api/knowledge'
import MarkdownRenderer from '@/components/MarkdownRenderer.vue'

const documents = ref<KbDocument[]>([])
const fileList = ref<UploadUserFile[]>([])
const question = ref('')
const answer = ref('')
const loading = ref(false)

async function loadDocuments() {
  documents.value = await knowledgeApi.list()
}

async function upload() {
  const file = fileList.value[0]?.raw
  if (!file) return
  loading.value = true
  try {
    await knowledgeApi.upload(file)
    ElMessage.success('上传成功，后台正在解析')
    fileList.value = []
    await loadDocuments()
  } finally {
    loading.value = false
  }
}

async function remove(id: number) {
  await knowledgeApi.delete(id)
  ElMessage.success('删除成功')
  await loadDocuments()
}

async function reembed(id: number) {
  await knowledgeApi.reembed(id)
  ElMessage.success('已触发重新向量化，稍后刷新查看状态')
  setTimeout(loadDocuments, 3000)
}

async function ask() {
  if (!question.value.trim()) return
  loading.value = true
  try {
    const response = await knowledgeApi.ask(question.value)
    if (!response?.success) throw new Error(response?.errorMessage || '问答失败')
    answer.value = response.content
  } finally {
    loading.value = false
  }
}

onMounted(loadDocuments)
</script>

<template>
  <div class="page-grid">
    <section class="panel">
      <div class="panel-title">知识库管理</div>
      <el-upload v-model:file-list="fileList" drag action="#" :auto-upload="false" :limit="1">
        <div class="el-upload__text">拖拽或选择知识库文档</div>
        <template #tip><div class="el-upload__tip">支持 pdf、docx、txt、md，单文件不超过 10MB。</div></template>
      </el-upload>
      <div class="mt-4 flex justify-end"><el-button type="primary" :loading="loading" @click="upload">上传解析</el-button></div>
    </section>

    <section class="panel">
      <div class="panel-title">文档列表</div>
      <el-table :data="documents" stripe>
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="fileType" label="类型" width="90" />
        <el-table-column prop="parseStatus" label="解析" width="100" />
        <el-table-column prop="embeddingStatus" label="向量化" width="110" />
        <el-table-column prop="chunkCount" label="切片数" width="90" />
        <el-table-column label="操作" width="170">
          <template #default="{ row }">
            <el-button link type="primary" @click="reembed(row.id)">重新向量化</el-button>
            <el-button link type="danger" @click="remove(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <section class="panel">
      <div class="panel-title">知识库问答测试</div>
      <el-input v-model="question" type="textarea" :rows="3" placeholder="输入就业政策、流程或知识库问题" />
      <div class="mt-4"><el-button type="primary" :loading="loading" @click="ask">提问</el-button></div>
      <el-alert v-if="answer" class="mt-4" type="success" :closable="false"><MarkdownRenderer :content="answer" /></el-alert>
    </section>
  </div>
</template>
