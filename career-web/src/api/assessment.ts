import { assessments, getAssessmentQuestions, getAssessmentResult } from '@/mock'
import type { Assessment, AssessmentQuestion, AssessmentResult } from '@/types'

export const assessmentApi = {
  list: (): Promise<Assessment[]> => Promise.resolve(assessments),

  questions: (id: number): Promise<AssessmentQuestion[]> => Promise.resolve(getAssessmentQuestions(id)),

  result: (id: number): Promise<AssessmentResult> => Promise.resolve(getAssessmentResult(id))
}
