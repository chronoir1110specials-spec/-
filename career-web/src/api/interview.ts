import { interviewFeedback, interviewHistory } from '@/mock'
import type { InterviewFeedback, InterviewSession } from '@/types'
import { get, post } from './client'

interface ChatResponse {
  success: boolean
  content: string
  modelName?: string
  errorMessage?: string
}

interface InterviewQuestionPayload {
  sessionId: number
  question: string
}

interface InterviewSummaryPayload {
  sessionId: number
  summary: string
}

function parseContent<T>(content: string | undefined): T | null {
  if (!content) return null
  try {
    const cleaned = content.replace(/```json\s*/gi, '').replace(/```/g, '').trim()
    return JSON.parse(cleaned) as T
  } catch {
    return null
  }
}

export const interviewApi = {
  history: () => Promise.resolve<InterviewSession[]>(interviewHistory),

  feedback: () => Promise.resolve<InterviewFeedback>(interviewFeedback),

  start: async (
    position: string,
    difficulty = 'normal',
    totalQuestions = 5
  ): Promise<InterviewQuestionPayload> => {
    const data = await post<ChatResponse>('/agent/interview/start', { position, difficulty, totalQuestions })
    if (!data || !data.success) throw new Error(data?.errorMessage || '开始面试失败')
    const parsed = parseContent<InterviewQuestionPayload>(data.content)
    if (!parsed) throw new Error('面试响应解析失败')
    return parsed
  },

  answer: async (sessionId: number, answer: string): Promise<ChatResponse> => {
    return post<ChatResponse>('/agent/interview/answer', { sessionId, answer })
  },

  next: async (sessionId: number): Promise<InterviewQuestionPayload> => {
    const data = await post<ChatResponse>('/agent/interview/next', { sessionId })
    if (!data || !data.success) throw new Error(data?.errorMessage || '获取下一题失败')
    const parsed = parseContent<InterviewQuestionPayload>(data.content)
    if (!parsed) throw new Error('面试响应解析失败')
    return parsed
  },

  status: async (sessionId: number) => {
    return get<Record<string, unknown>>(`/agent/interview/status/${sessionId}`)
  },

  summary: async (sessionId: number): Promise<InterviewSummaryPayload> => {
    const data = await get<ChatResponse>(`/agent/interview/summary/${sessionId}`)
    if (!data || !data.success) throw new Error(data?.errorMessage || '获取面试总结失败')
    const parsed = parseContent<InterviewSummaryPayload>(data.content)
    if (!parsed) throw new Error('总结响应解析失败')
    return parsed
  }
}
