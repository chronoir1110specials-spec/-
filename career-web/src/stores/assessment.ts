import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { assessments, getAssessmentQuestions } from '@/mock'

export const useAssessmentStore = defineStore('assessment', () => {
  const list = ref(assessments)
  const questions = ref(getAssessmentQuestions(1))
  const internalCurrentIndex = ref(0)
  const answers = ref<Record<number, number>>({})

  const currentIndex = computed(() => internalCurrentIndex.value)
  const progress = computed(() => {
    const questionCount = questions.value.length
    if (questionCount === 0) {
      return 0
    }
    return Math.round((Object.keys(answers.value).length / questionCount) * 100)
  })

  function load(assessmentId: number) {
    questions.value = getAssessmentQuestions(assessmentId)
    internalCurrentIndex.value = 0
    answers.value = {}
  }

  function answer(questionId: number, value: number) {
    answers.value[questionId] = value
  }

  function next() {
    if (internalCurrentIndex.value >= questions.value.length - 1) {
      return false
    }
    internalCurrentIndex.value += 1
    return true
  }

  function prev() {
    if (internalCurrentIndex.value <= 0) {
      return false
    }
    internalCurrentIndex.value -= 1
    return true
  }

  return { list, questions, currentIndex, answers, progress, load, answer, next, prev }
})
