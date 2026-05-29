import { courses, milestones } from '@/mock'
import type { Course, LearningMilestone } from '@/types'

export const learningApi = {
  overview: (): Promise<{ courses: Course[]; milestones: LearningMilestone[] }> =>
    Promise.resolve({ courses, milestones })
}
