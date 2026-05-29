import type { ResumeAnalysis, ResumeInfo, ResumeRecord } from '@/types'
import { del, get, http, post } from './client'

interface ChatResponse {
  success: boolean
  content: string
  modelName?: string
  errorMessage?: string
}

function safeJson<T>(text: string | undefined, fallback: T): T {
  if (!text) return fallback
  try {
    const cleaned = text.replace(/```json\s*/gi, '').replace(/```/g, '').trim()
    return JSON.parse(cleaned) as T
  } catch {
    return fallback
  }
}

function normalizeAnalysis(text?: string, score?: number): ResumeAnalysis {
  const parsed = safeJson<Record<string, unknown>>(text, {})
  const suggestions = (parsed.suggestions || parsed.resumeAdvice || []) as string[]
  const problems = (parsed.problems || parsed.issues || []) as string[]
  return {
    score: Number(parsed.score || score || 0),
    dimensions: Array.isArray(parsed.dimensions) ? (parsed.dimensions as ResumeAnalysis['dimensions']) : [],
    issues: problems,
    suggestions,
    summary: typeof parsed.summary === 'string' ? parsed.summary : text,
    problems,
    keywords: Array.isArray(parsed.keywords) ? (parsed.keywords as string[]) : []
  }
}

function toRecord(item: ResumeInfo, index: number): ResumeRecord {
  const status = item.parseStatus === 'failed' ? '解析失败' : item.score && item.score < 75 ? '需优化' : item.parseStatus === 'success' ? '已分析' : '分析中'
  return {
    id: item.id,
    fileName: item.originalFileName || item.resumeName || `简历 ${item.id}`,
    uploadTime: item.updateTime || item.createTime || '',
    score: item.score || 0,
    status,
    version: `V${index + 1}`,
    raw: item
  }
}

export const resumeApi = {
  history: async (): Promise<ResumeRecord[]> => {
    const list = await get<ResumeInfo[]>('/resume/list')
    return (list || []).map(toRecord)
  },

  latest: (): Promise<ResumeInfo | null> => get<ResumeInfo | null>('/resume/latest'),

  detail: (id: number): Promise<ResumeInfo> => get<ResumeInfo>(`/resume/${id}`),

  analysis: async (id?: number): Promise<ResumeAnalysis> => {
    const info = id ? await resumeApi.detail(id) : await resumeApi.latest()
    return normalizeAnalysis(info?.analysisResult, info?.score)
  },

  optimize: async (resumeContent: string, resumeName?: string): Promise<ResumeAnalysis> => {
    const data = await post<ChatResponse>('/agent/resume/optimize', { resumeContent, resumeName })
    if (!data || !data.success) throw new Error(data?.errorMessage || '简历分析失败')
    return normalizeAnalysis(data.content)
  },

  upload: async (file: File, onProgress?: (percentage: number) => void): Promise<ResumeInfo> => {
    const form = new FormData()
    form.append('file', file)
    return http
      .post('/resume/upload', form, {
        headers: { 'Content-Type': 'multipart/form-data' },
        onUploadProgress: (event) => {
          if (event.total && onProgress) onProgress(Math.round((event.loaded / event.total) * 100))
        }
      })
      .then((res) => res.data.data as ResumeInfo)
  },

  delete: (id: number): Promise<boolean> => del<boolean>(`/resume/delete/${id}`)
}
