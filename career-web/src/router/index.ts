import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'
import { getToken } from '@/api/client'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'login', component: () => import('@/views/auth/LoginView.vue'), meta: { title: '登录', public: true } },
    { path: '/register', name: 'register', component: () => import('@/views/auth/RegisterView.vue'), meta: { title: '注册', public: true } },
    {
      path: '/',
      component: MainLayout,
      redirect: '/home',
      meta: { requiresAuth: true },
      children: [
        { path: 'home', name: 'home', component: () => import('@/views/home/HomeView.vue'), meta: { title: '首页', requiresAuth: true } },
        { path: 'assessment', name: 'assessment', component: () => import('@/views/assessment/AssessmentListView.vue'), meta: { title: '职业测评', requiresAuth: true } },
        { path: 'assessment/:id', name: 'assessment-detail', component: () => import('@/views/assessment/AssessmentDetailView.vue'), meta: { title: '测评答题', requiresAuth: true } },
        { path: 'assessment/:id/result', name: 'assessment-result', component: () => import('@/views/assessment/AssessmentResultView.vue'), meta: { title: '测评结果', requiresAuth: true } },
        { path: 'resume', redirect: '/resume/upload' },
        { path: 'resume/upload', name: 'resume-upload', component: () => import('@/views/resume/ResumeUploadView.vue'), meta: { title: '简历上传', requiresAuth: true } },
        { path: 'resume/analysis', name: 'resume-analysis', component: () => import('@/views/resume/ResumeAnalysisView.vue'), meta: { title: '简历分析', requiresAuth: true } },
        { path: 'resume/history', name: 'resume-history', component: () => import('@/views/resume/ResumeHistoryView.vue'), meta: { title: '历史简历', requiresAuth: true } },
        { path: 'chat', name: 'chat', component: () => import('@/views/chat/ChatView.vue'), meta: { title: '智能对话', requiresAuth: true } },
        { path: 'interview/room', name: 'interview-room', component: () => import('@/views/interview/InterviewRoomView.vue'), meta: { title: '面试训练', requiresAuth: true } },
        { path: 'interview/feedback', name: 'interview-feedback', component: () => import('@/views/interview/InterviewFeedbackView.vue'), meta: { title: '面试反馈', requiresAuth: true } },
        { path: 'interview/history', name: 'interview-history', component: () => import('@/views/interview/InterviewHistoryView.vue'), meta: { title: '面试历史', requiresAuth: true } },
        { path: 'jobs', name: 'jobs', component: () => import('@/views/jobs/JobMatchView.vue'), meta: { title: '岗位匹配', requiresAuth: true } },
        { path: 'jobs/:id', name: 'job-detail', component: () => import('@/views/jobs/JobDetailView.vue'), meta: { title: '岗位详情', requiresAuth: true } },
        { path: 'learning', name: 'learning', component: () => import('@/views/learning/LearningPathView.vue'), meta: { title: '学习路径', requiresAuth: true } },
        { path: 'profile', name: 'profile', component: () => import('@/views/profile/ProfileView.vue'), meta: { title: '个人中心', requiresAuth: true } },
        { path: 'knowledge', name: 'knowledge', component: () => import('@/views/knowledge/KnowledgeView.vue'), meta: { title: '知识库管理', requiresAuth: true, roles: ['admin'] } },
        { path: 'admin/model', name: 'admin-model', component: () => import('@/views/admin/ModelConfigView.vue'), meta: { title: '模型配置', requiresAuth: true, roles: ['admin'] } },
        { path: 'admin/logs', name: 'admin-logs', component: () => import('@/views/admin/LogView.vue'), meta: { title: '运行日志', requiresAuth: true, roles: ['admin'] } },
        { path: ':pathMatch(.*)*', name: 'not-found', component: () => import('@/views/NotFoundView.vue'), meta: { title: '页面不存在', requiresAuth: true } }
      ]
    }
  ]
})

router.beforeEach(async (to) => {
  if (to.meta.public) return true
  if (!getToken()) return { path: '/login', query: { redirect: to.fullPath } }
  const userStore = useUserStore()
  if (!userStore.profile.userId && !userStore.profile.id) await userStore.loadProfile().catch(() => null)
  const roles = (to.meta.roles as string[] | undefined) || []
  if (roles.length && !roles.some((role) => userStore.hasRole(role))) return '/home'
  return true
})

export default router
