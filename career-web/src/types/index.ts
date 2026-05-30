export interface UserProfile {
  id?: number
  userId?: number
  school?: string
  major?: string
  grade?: string
  targetPosition?: string
  targetCity?: string
  skillTags?: string
  projectTags?: string
  jobStage?: string
  createTime?: string
  updateTime?: string
  deleted?: number
  name?: string
  avatar?: string
  title?: string
  email?: string
  phone?: string
  city?: string
  targetRole?: string
  bio?: string
  notifications?: {
    jobAlert: boolean
    courseReminder: boolean
    interviewFeedback: boolean
  }
}

export interface DashboardStat {
  label: string
  value: string | number
  trend: string
  type: 'primary' | 'success' | 'warning' | 'danger'
}

export interface ActivityItem {
  id: number
  time: string
  title: string
  content: string
  type: 'primary' | 'success' | 'warning' | 'info'
}

export interface Assessment {
  id: number
  name: string
  category: string
  description: string
  duration: number
  questionCount: number
  completedCount: number
  tags: string[]
}

export interface AssessmentQuestion {
  id: number
  assessmentId: number
  text: string
  options: Array<{ label: string; value: number }>
}

export interface AssessmentResult {
  assessmentId: number
  title: string
  summary: string
  scores: Array<{ name: string; score: number }>
  careers: Array<{ title: string; reason: string; match: number }>
}

export interface ResumeInfo {
  id: number
  userId?: number
  resumeName?: string
  originalFileName?: string
  fileType?: string
  fileUrl?: string
  contentHash?: string
  content?: string
  parseStatus?: string
  parseError?: string
  targetPosition?: string
  analysisResult?: string
  score?: number
  createTime?: string
  updateTime?: string
  deleted?: number
}

export interface ResumeRecord {
  id: number
  fileName: string
  uploadTime: string
  score: number
  status: '已分析' | '分析中' | '需优化' | '解析失败'
  version: string
  raw?: ResumeInfo
}

export interface ResumeAnalysis {
  score: number
  dimensions: Array<{ name: string; score: number; comment: string }>
  issues: string[]
  suggestions: string[]
  summary?: string
  problems?: string[]
  keywords?: string[]
}

export interface InterviewSession {
  id: number
  role: string
  company: string
  time: string
  score: number
  status: '已完成' | '进行中'
}

export interface InterviewQuestion {
  id: number
  question: string
  answer: string
  feedback: string
  score: number
}

export interface InterviewFeedback {
  overall: number
  dimensions: Array<{ name: string; score: number }>
  questions: InterviewQuestion[]
  suggestions: string[]
}

export interface JobInfo {
  id: number
  userId?: number
  jobName?: string
  companyName?: string
  jobDescription?: string
  analysisResult?: string
  matchScore?: number
  createTime?: string
  deleted?: number
}

export interface Job {
  id: number
  title: string
  company: string
  city: string
  salary: string
  match: number
  tags: string[]
  description: string
  requirements: string[]
  highlights: string[]
  gaps: string[]
  raw?: JobInfo
}

export interface ChatSession {
  id: number
  userId?: number
  title?: string
  sessionType?: string
  summary?: string
  createTime?: string
  updateTime?: string
}

export interface ChatMessage {
  id: number
  sessionId: number
  userId?: number
  role: 'user' | 'assistant' | 'system' | string
  content: string
  modelName?: string
  createTime?: string
}

export interface KbDocument {
  id: number
  title?: string
  fileName?: string
  fileType?: string
  parseStatus?: string
  embeddingStatus?: string
  chunkCount?: number
  createUser?: number
  createTime?: string
}

export interface AgentTask {
  id: number
  userId?: number
  sessionId?: number
  agentType?: string
  taskType?: string
  executionMode?: string
  inputSummary?: string
  outputSummary?: string
  status?: string
  currentStep?: string
  totalTokens?: number
  totalCostTime?: number
  errorMessage?: string
  createTime?: string
  finishTime?: string
}

export interface AgentStepLog {
  id: number
  taskId?: number
  stepIndex?: number
  stepType?: string
  stepName?: string
  inputSummary?: string
  outputSummary?: string
  status?: string
  errorMessage?: string
  createTime?: string
}

export interface ModelConfig {
  id?: number
  modelRole?: string
  provider?: string
  modelName?: string
  baseUrl?: string
  apiKey?: string
  enabled?: number
  maxTokens?: number
  embeddingDimension?: number
  timeout?: number
  createTime?: string
}

export interface ModelCallLog {
  id: number
  userId?: number
  modelRole?: string
  modelName?: string
  provider?: string
  promptTokens?: number
  completionTokens?: number
  totalTokens?: number
  costTime?: number
  success?: number
  isFallback?: number
  errorMessage?: string
  createTime?: string
}

export interface ModelTestResult {
  modelRole?: string
  reachable?: boolean
  configured?: boolean
  provider?: string
  modelName?: string
  reply?: string
  costTime?: number
  totalTokens?: number
  errorMessage?: string
}

export interface Course {
  id: number
  title: string
  provider: string
  progress: number
  level: string
}

export interface LearningMilestone {
  id: number
  title: string
  date: string
  status: 'finish' | 'process' | 'wait'
}
