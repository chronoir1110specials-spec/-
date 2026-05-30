import type { AgentStepLog, AgentTask, ModelCallLog, ModelConfig, ModelTestResult } from '@/types'
import { get, post } from './client'

export interface AdminStats {
  totalUsers?: number
  totalSessions?: number
  totalMessages?: number
  totalDocuments?: number
}

export interface AgentToolInfo {
  name: string
  description: string
  readOnly: boolean
  destructive: boolean
}

export interface AgentDefInfo {
  agentType: string
  displayName: string
  allowedTools: string[]
  ragPolicy: string
  memoryPolicy: string
  modelPolicy: string
  maxToolCalls: number
}

export interface AgentCapabilities {
  tools: AgentToolInfo[]
  agents: AgentDefInfo[]
}

export const adminApi = {
  stats: (): Promise<AdminStats> => get<AdminStats>('/admin/stats'),
  models: (): Promise<ModelConfig[]> => get<ModelConfig[]>('/admin/model/list'),
  saveModel: (config: ModelConfig): Promise<ModelConfig> => post<ModelConfig>('/admin/model/save', config),
  modelLogs: (limit = 50): Promise<ModelCallLog[]> => get<ModelCallLog[]>(`/admin/log/list?limit=${limit}`),
  agentLogs: (limit = 50): Promise<AgentTask[]> => get<AgentTask[]>(`/admin/log/agent?limit=${limit}`),
  agentSteps: (taskId: number): Promise<AgentStepLog[]> => get<AgentStepLog[]>(`/admin/log/agent/steps/${taskId}`),
  testPrimaryModel: (): Promise<ModelTestResult> => get<ModelTestResult>('/admin/model/test/primary'),
  testFallbackModel: (): Promise<ModelTestResult> => get<ModelTestResult>('/admin/model/test/fallback'),
  getRateLimit: (): Promise<{ dailyLimit: number }> => get<{ dailyLimit: number }>('/admin/config/rate-limit'),
  setRateLimit: (dailyLimit: number): Promise<boolean> => post<boolean>('/admin/config/rate-limit', { dailyLimit }),
  capabilities: (): Promise<AgentCapabilities> => get<AgentCapabilities>('/agent/tools')
}
