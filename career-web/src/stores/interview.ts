import { defineStore } from 'pinia'
import { ref } from 'vue'
import { interviewFeedback, interviewHistory } from '@/mock'

export const useInterviewStore = defineStore('interview', () => {
  const history = ref(interviewHistory)
  const feedback = ref(interviewFeedback)
  const remainingSeconds = ref(15 * 60)

  function tick() {
    remainingSeconds.value = Math.max(0, remainingSeconds.value - 1)
  }

  function reset() {
    remainingSeconds.value = 15 * 60
  }

  return { history, feedback, remainingSeconds, tick, reset }
})
