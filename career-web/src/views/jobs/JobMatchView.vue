<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { Job } from '@/types'
import { jobsApi } from '@/api/jobs'

const keyword = ref('')
const jobs = ref<Job[]>([])
const analyzing = ref(false)
const form = reactive({ jobName: '', companyName: '', jobDescription: '' })

const sortedJobs = computed(() =>
  jobs.value
    .filter((job) => `${job.title}${job.company}${job.tags.join('')}`.includes(keyword.value))
    .sort((a, b) => b.match - a.match)
)

async function loadJobs() {
  jobs.value = await jobsApi.list()
}

async function analyze() {
  if (!form.jobDescription.trim()) {
    ElMessage.warning('请粘贴岗位 JD')
    return
  }
  analyzing.value = true
  try {
    const response = await jobsApi.analyze(form.jobDescription, form.jobName, form.companyName)
    if (!response?.success) throw new Error(response?.errorMessage || '岗位分析失败')
    ElMessage.success('岗位分析已生成')
    form.jobName = ''
    form.companyName = ''
    form.jobDescription = ''
    await loadJobs()
  } finally {
    analyzing.value = false
  }
}

onMounted(loadJobs)
</script>

<template>
  <div class="page-grid">
    <section class="panel">
      <div class="panel-title">新增岗位分析</div>
      <div class="grid gap-4 md:grid-cols-2">
        <el-input v-model="form.jobName" placeholder="岗位名称" />
        <el-input v-model="form.companyName" placeholder="公司名称" />
      </div>
      <el-input v-model="form.jobDescription" class="mt-4" type="textarea" :rows="6" placeholder="粘贴岗位 JD，系统会分析技能要求、匹配度和简历优化方向" />
      <div class="mt-4 flex justify-end"><el-button type="primary" :loading="analyzing" @click="analyze">开始分析</el-button></div>
    </section>

    <section class="panel">
      <div class="panel-title">岗位匹配历史</div>
      <div class="flex flex-wrap gap-3">
        <el-input v-model="keyword" class="max-w-xl" :prefix-icon="Search" placeholder="搜索岗位、公司或技能关键词" clearable />
        <el-select model-value="match" class="w-44">
          <el-option label="按匹配度排序" value="match" />
        </el-select>
      </div>
    </section>

    <section class="grid gap-4">
      <el-empty v-if="sortedJobs.length === 0" description="暂无岗位分析记录" />
      <el-card v-for="job in sortedJobs" :key="job.id" shadow="never">
        <div class="flex flex-wrap items-start justify-between gap-4">
          <div>
            <h3 class="text-xl font-semibold text-slate-800">{{ job.title }}</h3>
            <p class="muted mt-2">{{ job.company }} · {{ job.city }} · {{ job.salary }}</p>
            <p class="mt-3 line-clamp-3 text-slate-500">{{ job.description }}</p>
            <div class="mt-4 flex flex-wrap gap-2">
              <el-tag v-for="tag in job.tags" :key="tag">{{ tag }}</el-tag>
            </div>
          </div>
          <div class="flex min-w-40 flex-col items-end gap-3">
            <el-progress type="circle" :percentage="job.match" :width="76" />
            <el-button type="primary" @click="$router.push(`/jobs/${job.id}`)">查看详情</el-button>
          </div>
        </div>
      </el-card>
    </section>
  </div>
</template>
