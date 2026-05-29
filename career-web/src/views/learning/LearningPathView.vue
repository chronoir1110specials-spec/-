<script setup lang="ts">
import { courses, milestones } from '@/mock'
</script>

<template>
  <div class="page-grid">
    <section class="panel">
      <div class="panel-title">技能图谱</div>
      <div class="overflow-auto">
        <svg viewBox="0 0 860 300" class="h-[300px] min-w-[760px] w-full">
          <defs>
            <filter id="soft"><feDropShadow dx="0" dy="8" stdDeviation="8" flood-color="#000" flood-opacity=".28" /></filter>
          </defs>
          <g stroke="#38bdf8" stroke-width="2" opacity=".7">
            <line x1="430" y1="70" x2="220" y2="170" />
            <line x1="430" y1="70" x2="430" y2="190" />
            <line x1="430" y1="70" x2="640" y2="170" />
            <line x1="220" y1="170" x2="430" y2="240" />
            <line x1="640" y1="170" x2="430" y2="240" />
          </g>
          <g filter="url(#soft)" font-size="16" font-weight="700" text-anchor="middle">
            <rect x="340" y="32" width="180" height="76" rx="8" fill="#2563eb" />
            <text x="430" y="76" fill="#fff">前端工程能力</text>
            <rect x="120" y="134" width="200" height="76" rx="8" fill="#0f766e" />
            <text x="220" y="178" fill="#fff">Vue / TypeScript</text>
            <rect x="330" y="154" width="200" height="76" rx="8" fill="#7c3aed" />
            <text x="430" y="198" fill="#fff">工程化与测试</text>
            <rect x="540" y="134" width="200" height="76" rx="8" fill="#c2410c" />
            <text x="640" y="178" fill="#fff">性能与监控</text>
            <rect x="330" y="222" width="200" height="58" rx="8" fill="#334155" />
            <text x="430" y="257" fill="#fff">系统设计表达</text>
          </g>
        </svg>
      </div>
    </section>

    <section class="grid gap-4 lg:grid-cols-[1fr_380px]">
      <div class="panel">
        <div class="panel-title">推荐课程</div>
        <el-card v-for="course in courses" :key="course.id" class="mb-4 last:mb-0" shadow="never">
          <div class="mb-3 flex items-center justify-between">
            <div>
              <strong>{{ course.title }}</strong>
              <p class="muted mt-1">{{ course.provider }} · {{ course.level }}</p>
            </div>
            <el-button type="primary">继续学习</el-button>
          </div>
          <el-progress :percentage="course.progress" />
        </el-card>
      </div>

      <div class="panel">
        <div class="panel-title">学习时间线</div>
        <el-steps direction="vertical" :active="2" finish-status="success" process-status="process">
          <el-step v-for="item in milestones" :key="item.id" :title="item.title" :description="item.date" :status="item.status" />
        </el-steps>
      </div>
    </section>
  </div>
</template>
