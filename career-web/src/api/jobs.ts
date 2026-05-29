import type { Job, JobInfo } from '@/types'
import { del, get, post } from './client'

interface ChatResponse {
  success: boolean
  content: string
  modelName?: string
  errorMessage?: string
}

function safeJson(text?: string): Record<string, unknown> {
  if (!text) return {}
  try {
    return JSON.parse(text.replace(/```json\s*/gi, '').replace(/```/g, '').trim()) as Record<string, unknown>
  } catch {
    return {}
  }
}

function toJob(item: JobInfo): Job {
  const analysis = safeJson(item.analysisResult)
  return {
    id: item.id,
    title: item.jobName || '未命名岗位',
    company: item.companyName || '未填写公司',
    city: '未填写',
    salary: '未填写',
    match: item.matchScore || Number(analysis.matchScore || 0),
    tags: ((analysis.requiredSkills || analysis.bonusSkills || []) as string[]).slice(0, 6),
    description: item.jobDescription || '',
    requirements: ((analysis.requiredSkills || []) as string[]),
    highlights: ((analysis.bonusSkills || analysis.interviewTopics || []) as string[]),
    gaps: ((analysis.resumeAdvice || []) as string[]),
    raw: item
  }
}

export const jobsApi = {
  list: async (): Promise<Job[]> => {
    const list = await get<JobInfo[]>('/agent/job/list')
    return (list || []).map(toJob)
  },

  detail: async (id: number): Promise<Job> => toJob(await get<JobInfo>(`/agent/job/${id}`)),

  delete: (id: number): Promise<boolean> => del<boolean>(`/agent/job/delete/${id}`),

  analyze: async (jobDescription: string, jobName?: string, companyName?: string): Promise<ChatResponse> => {
    return post<ChatResponse>('/agent/job/analyze', { jobDescription, jobName, companyName })
  }
}
