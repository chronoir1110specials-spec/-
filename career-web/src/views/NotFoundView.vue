<template>
  <section class="panel mx-auto max-w-2xl text-center">
    <div class="panel-title justify-center">页面不存在</div>
    <p class="muted">你访问的页面不存在或已被移动。</p>
    <el-button class="mt-6" type="primary" @click="$router.push('/home')">返回首页</el-button>
  </section>
</template>
