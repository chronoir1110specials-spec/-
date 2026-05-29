import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { ResumeAnalysis, ResumeRecord } from '@/types'
import { resumeApi } from '@/api/resume'

const emptyAnalysis: ResumeAnalysis = { score: 0, dimensions: [], issues: [], suggestions: [] }

export const useResumeStore = defineStore('resume', () => {
  const history = ref<ResumeRecord[]>([])
  const analysis = ref<ResumeAnalysis>({ ...emptyAnalysis })
  const uploadProgress = ref(0)

  function setUploadProgress(value: number) {
    uploadProgress.value = value
  }

  async function loadHistory() {
    history.value = await resumeApi.history()
    return history.value
  }

  async function loadAnalysis(id?: number) {
    analysis.value = await resumeApi.analysis(id)
    return analysis.value
  }

  return { history, analysis, uploadProgress, setUploadProgress, loadHistory, loadAnalysis }
})
