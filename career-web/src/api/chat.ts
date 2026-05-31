import type { ChatMessage, ChatSession } from '@/types'
import { del, get, getToken, post, put } from './client'

interface ChatResponse {
  success: boolean
  content: string
  modelName?: string
  errorMessage?: string
}

interface ChatDetail {
  session: ChatSession
  messages: ChatMessage[]
}

export interface StreamHandlers {
  onStart?: (sessionId: number) => void
  onDelta: (piece: string) => void
  onDone?: (meta: { modelName?: string; totalTokens?: number | null }) => void
  onError?: (message: string) => void
}

export const chatApi = {
  sessions: (): Promise<ChatSession[]> => get<ChatSession[]>('/chat/session/list'),
  createSession: (title?: string): Promise<ChatSession> => post<ChatSession>('/chat/session/create', { title }),
  detail: (id: number): Promise<ChatDetail> => get<ChatDetail>(`/chat/session/${id}`),
  messages: (id: number): Promise<ChatMessage[]> => get<ChatMessage[]>(`/chat/session/${id}/messages`),
  send: (sessionId: number, content: string): Promise<ChatResponse> => post<ChatResponse>(`/chat/session/${sessionId}/messages`, { content }),
  deleteSession: (id: number): Promise<boolean> => del<boolean>(`/chat/session/${id}`),
  renameSession: (id: number, title: string): Promise<boolean> => put<boolean>(`/chat/session/${id}/title`, { title }),

  /**
   * 流式发送（SSE）。用 fetch 读取响应流，按 SSE 协议逐块回调。
   */
  async stream(sessionId: number, content: string, handlers: StreamHandlers): Promise<void> {
    const token = getToken()
    let resp: Response
    try {
      resp = await fetch(`/api/chat/session/${sessionId}/messages/stream`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Accept: 'text/event-stream',
          ...(token ? { Authorization: `Bearer ${token}` } : {})
        },
        body: JSON.stringify({ content })
      })
    } catch (e) {
      handlers.onError?.((e as Error).message || '网络异常')
      return
    }
    if (!resp.ok || !resp.body) {
      handlers.onError?.(`连接失败 (${resp.status})`)
      return
    }
    const reader = resp.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''
    let eventName = 'message'
    const dataLines: string[] = []

    const dispatch = () => {
      if (dataLines.length === 0) {
        eventName = 'message'
        return
      }
      const data = dataLines.join('\n')
      dataLines.length = 0
      if (eventName === 'delta') {
        handlers.onDelta(data)
      } else if (eventName === 'start') {
        handlers.onStart?.(sessionId)
      } else if (eventName === 'done') {
        let meta: { modelName?: string; totalTokens?: number | null } = {}
        try { meta = JSON.parse(data) } catch { /* ignore */ }
        handlers.onDone?.(meta)
      } else if (eventName === 'error') {
        handlers.onError?.(data)
      }
      eventName = 'message'
    }

    for (;;) {
      const { value, done } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() ?? ''
      for (const raw of lines) {
        const line = raw.replace(/\r$/, '')
        if (line === '') {
          dispatch()
        } else if (line.startsWith('event:')) {
          eventName = line.slice(6).trim()
        } else if (line.startsWith('data:')) {
          dataLines.push(line.slice(5).replace(/^ /, ''))
        }
      }
    }
    dispatch()
  }
}
