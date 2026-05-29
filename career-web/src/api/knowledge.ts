import type { KbDocument } from '@/types'
import { del, get, http, post } from './client'

interface AskResponse {
  success: boolean
  content: string
  modelName?: string
  errorMessage?: string
}

export const knowledgeApi = {
  list: (): Promise<KbDocument[]> => get<KbDocument[]>('/kb/document/list'),
  upload: (file: File): Promise<KbDocument> => {
    const form = new FormData()
    form.append('file', file)
    return http.post('/kb/document/upload', form, { headers: { 'Content-Type': 'multipart/form-data' } })
      .then((res) => res.data.data as KbDocument)
  },
  delete: (id: number): Promise<boolean> => del<boolean>(`/kb/document/delete/${id}`),
  reembed: (id: number): Promise<boolean> => post<boolean>(`/kb/document/${id}/reembed`),
  ask: (question: string): Promise<AskResponse> => post<AskResponse>('/kb/ask', { question })
}
