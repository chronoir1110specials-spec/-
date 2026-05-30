import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useInterviewStore = defineStore('interview', () => {
  const remainingSeconds = ref(15 * 60)
  // 面试结束后由房间写入真实总结文本，供反馈页展示
  const summaryText = ref('')

  function tick() {
    remainingSeconds.value = Math.max(0, remainingSeconds.value - 1)
  }

  function reset() {
    remainingSeconds.value = 15 * 60
  }

  return { remainingSeconds, summaryText, tick, reset }
})
