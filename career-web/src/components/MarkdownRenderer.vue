<script setup lang="ts">
import { computed } from 'vue'
import MarkdownIt from 'markdown-it'
import DOMPurify from 'dompurify'

const props = defineProps<{ content: string }>()

const md = new MarkdownIt({
  html: false, // 不信任原始 HTML，配合 DOMPurify 双重防护
  linkify: true,
  breaks: true
})

const rendered = computed(() => {
  const raw = md.render(props.content || '')
  // XSS 净化：清理脚本/事件属性等（设计 15.7）
  return DOMPurify.sanitize(raw, { ADD_ATTR: ['target'] })
})
</script>

<template>
  <div class="markdown-body" v-html="rendered" />
</template>

<style scoped>
.markdown-body {
  line-height: 1.7;
  word-break: break-word;
}
.markdown-body :deep(p) { margin: 0 0 8px; }
.markdown-body :deep(p:last-child) { margin-bottom: 0; }
.markdown-body :deep(ul),
.markdown-body :deep(ol) { padding-left: 20px; margin: 6px 0; }
.markdown-body :deep(code) {
  background: #f0f2f5;
  padding: 1px 5px;
  border-radius: 4px;
  font-size: 0.9em;
}
.markdown-body :deep(pre) {
  background: #f6f8fa;
  border: 1px solid #e9ecf2;
  padding: 12px;
  border-radius: 8px;
  overflow-x: auto;
}
.markdown-body :deep(pre code) { background: none; padding: 0; }
.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3) { margin: 12px 0 8px; font-weight: 600; }
.markdown-body :deep(a) { color: #0d9488; text-decoration: underline; }
.markdown-body :deep(blockquote) {
  border-left: 3px solid #d4d9e2;
  padding-left: 12px;
  color: #8a909c;
  margin: 8px 0;
}
.markdown-body :deep(table) { border-collapse: collapse; margin: 8px 0; }
.markdown-body :deep(th),
.markdown-body :deep(td) { border: 1px solid #e9ecf2; padding: 4px 8px; }
</style>
