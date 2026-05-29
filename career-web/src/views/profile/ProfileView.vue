<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const form = reactive({
  school: '',
  major: '',
  grade: '',
  targetPosition: '',
  targetCity: '',
  skillTags: '',
  projectTags: '',
  jobStage: ''
})

function assignForm() {
  Object.assign(form, {
    school: userStore.profile.school || '',
    major: userStore.profile.major || '',
    grade: userStore.profile.grade || '',
    targetPosition: userStore.profile.targetPosition || '',
    targetCity: userStore.profile.targetCity || '',
    skillTags: userStore.profile.skillTags || '',
    projectTags: userStore.profile.projectTags || '',
    jobStage: userStore.profile.jobStage || ''
  })
}

async function saveProfile() {
  await userStore.updateProfile({ ...form })
  ElMessage.success('画像已保存')
}

onMounted(async () => {
  await userStore.loadProfile()
  assignForm()
})
</script>

<template>
  <div class="grid gap-4 xl:grid-cols-[1fr_420px]">
    <section class="panel">
      <div class="panel-title">求职画像</div>
      <el-form label-width="96px" :model="form">
        <div class="grid gap-x-6 md:grid-cols-2">
          <el-form-item label="学校"><el-input v-model="form.school" /></el-form-item>
          <el-form-item label="专业"><el-input v-model="form.major" /></el-form-item>
          <el-form-item label="年级"><el-input v-model="form.grade" placeholder="如：大三 / 研一" /></el-form-item>
          <el-form-item label="目标岗位"><el-input v-model="form.targetPosition" /></el-form-item>
          <el-form-item label="目标城市"><el-input v-model="form.targetCity" /></el-form-item>
          <el-form-item label="求职阶段"><el-input v-model="form.jobStage" placeholder="准备简历 / 投递中 / 面试中" /></el-form-item>
        </div>
        <el-form-item label="技能标签">
          <el-input v-model="form.skillTags" type="textarea" :rows="3" placeholder="用逗号分隔，如 Java, Spring Boot, Vue" />
        </el-form-item>
        <el-form-item label="项目标签">
          <el-input v-model="form.projectTags" type="textarea" :rows="3" placeholder="用逗号分隔项目方向或亮点" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveProfile">保存画像</el-button>
        </el-form-item>
      </el-form>
    </section>

    <aside class="page-grid">
      <section class="panel">
        <div class="panel-title">画像概览</div>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="学校">{{ userStore.profile.school || '-' }}</el-descriptions-item>
          <el-descriptions-item label="专业">{{ userStore.profile.major || '-' }}</el-descriptions-item>
          <el-descriptions-item label="目标岗位">{{ userStore.profile.targetPosition || '-' }}</el-descriptions-item>
          <el-descriptions-item label="求职阶段">{{ userStore.profile.jobStage || '-' }}</el-descriptions-item>
        </el-descriptions>
      </section>

      <section class="panel">
        <div class="panel-title">使用说明</div>
        <p class="muted">画像会自动注入简历优化、岗位分析和智能对话 Prompt，让模型回答更贴合你的专业、技能和目标岗位。</p>
      </section>
    </aside>
  </div>
</template>
