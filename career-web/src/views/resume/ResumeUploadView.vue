<script setup lang="ts">
import { ref } from 'vue'
import { UploadFilled } from '@element-plus/icons-vue'
import { ElMessage, type UploadUserFile } from 'element-plus'
import { useRouter } from 'vue-router'
import { resumeApi } from '@/api/resume'
import { useResumeStore } from '@/stores/resume'

const router = useRouter()
const store = useResumeStore()
const uploading = ref(false)
const fileList = ref<UploadUserFile[]>([])

async function uploadResume() {
  const file = fileList.value[0]?.raw
  if (!file || uploading.value) return
  uploading.value = true
  store.setUploadProgress(0)
  try {
    const resume = await resumeApi.upload(file, (percentage) => store.setUploadProgress(percentage))
    await store.loadHistory()
    ElMessage.success(resume.parseStatus === 'failed' ? '上传成功，但解析失败，请查看详情' : '上传并解析成功')
    router.push({ path: '/resume/analysis', query: { id: resume.id } })
  } finally {
    uploading.value = false
  }
}
</script>

<template>
  <section class="panel mx-auto max-w-3xl">
    <div class="panel-title">简历上传</div>
    <el-upload v-model:file-list="fileList" drag action="#" :auto-upload="false" :limit="1" accept=".pdf,.docx,.txt">
      <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
      <div class="el-upload__text">拖拽简历到此处，或点击选择文件</div>
      <template #tip>
        <div class="el-upload__tip">支持 PDF、DOCX、TXT，文件小于 10MB。</div>
      </template>
    </el-upload>
    <div class="mt-6 flex justify-end">
      <el-button type="primary" :disabled="fileList.length === 0 || uploading" :loading="uploading" @click="uploadResume">
        上传并解析
      </el-button>
    </div>
    <div v-if="uploading || store.uploadProgress > 0" class="mt-6">
      <div class="mb-2 flex justify-between">
        <span>上传进度</span>
        <span>{{ store.uploadProgress }}%</span>
      </div>
      <el-progress :percentage="store.uploadProgress" :stroke-width="12" />
    </div>
  </section>
</template>
