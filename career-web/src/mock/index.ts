import type {
  ActivityItem,
  Assessment,
  AssessmentQuestion,
  AssessmentResult,
  Course,
  DashboardStat,
  InterviewFeedback,
  InterviewSession,
  Job,
  LearningMilestone,
  ResumeAnalysis,
  ResumeRecord,
  UserProfile
} from '@/types'

export const userProfile: UserProfile = {
  id: 1,
  name: '林晨',
  avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
  title: '应届生 / 前端开发方向',
  email: 'linchen@example.com',
  phone: '138-0000-2026',
  city: '杭州',
  school: '浙江大学',
  major: '软件工程',
  targetRole: '前端工程师',
  bio: '关注用户体验与工程质量，希望进入互联网产品研发团队。',
  notifications: { jobAlert: true, courseReminder: true, interviewFeedback: true }
}

export const dashboardStats: DashboardStat[] = [
  { label: '测评完成数', value: 6, trend: '本周 +2', type: 'primary' },
  { label: '简历评分', value: 86, trend: '较上版 +12', type: 'success' },
  { label: '面试次数', value: 14, trend: '平均分 82', type: 'warning' },
  { label: '匹配岗位数', value: 128, trend: '高匹配 23', type: 'danger' }
]

export const activities: ActivityItem[] = [
  { id: 1, time: '09:30', title: '完成 MBTI 测评', content: '结果为 ENFJ，适合产品协同与用户研究相关岗位。', type: 'success' },
  { id: 2, time: '昨天', title: '更新前端简历', content: 'Agent 识别到项目量化指标仍可加强。', type: 'primary' },
  { id: 3, time: '周一', title: '模拟面试反馈', content: '行为面试表达更稳定，技术深挖建议补充性能优化案例。', type: 'warning' }
]

export const assessments: Assessment[] = [
  { id: 1, name: '霍兰德职业兴趣测评', category: '兴趣倾向', description: '识别 RIASEC 六型兴趣组合，定位适配职业环境。', duration: 18, questionCount: 6, completedCount: 1842, tags: ['RIASEC', '职业方向'] },
  { id: 2, name: 'MBTI 性格测评', category: '性格风格', description: '评估沟通、决策和协作偏好，辅助岗位选择。', duration: 15, questionCount: 6, completedCount: 2531, tags: ['性格', '团队协作'] },
  { id: 3, name: 'DISC 行为风格测评', category: '行为风格', description: '识别支配、影响、稳健和谨慎偏好，优化沟通协作策略。', duration: 12, questionCount: 6, completedCount: 967, tags: ['DISC', '沟通风格'] },
  { id: 4, name: '核心能力雷达', category: '能力模型', description: '从学习力、执行力、沟通力等维度形成成长建议。', duration: 10, questionCount: 6, completedCount: 1260, tags: ['能力', '成长'] }
]

const agreeOptions = [
  { label: '非常不同意', value: 1 },
  { label: '不同意', value: 2 },
  { label: '一般', value: 3 },
  { label: '同意', value: 4 },
  { label: '非常同意', value: 5 }
]

export const assessmentQuestions: AssessmentQuestion[] = [
  { id: 101, assessmentId: 1, text: '我享受通过数据和事实拆解问题。', options: agreeOptions },
  { id: 102, assessmentId: 1, text: '我愿意动手操作工具、设备或完成具体产出。', options: agreeOptions },
  { id: 103, assessmentId: 1, text: '相比重复执行明确任务，我更喜欢探索新的解决方案。', options: agreeOptions },
  { id: 104, assessmentId: 1, text: '我喜欢和人沟通，帮助他人理解问题或做出选择。', options: agreeOptions },
  { id: 105, assessmentId: 1, text: '我对组织资源、推动项目和影响决策有兴趣。', options: agreeOptions },
  { id: 106, assessmentId: 1, text: '我在规则清晰、流程稳定的环境中更容易发挥。', options: agreeOptions },
  { id: 201, assessmentId: 2, text: '社交活动后，我通常会感到精力被补充。', options: agreeOptions },
  { id: 202, assessmentId: 2, text: '做决定时，我更看重客观逻辑而不是关系感受。', options: agreeOptions },
  { id: 203, assessmentId: 2, text: '我倾向于提前规划任务，而不是临场调整。', options: agreeOptions },
  { id: 204, assessmentId: 2, text: '面对信息时，我更关注整体可能性而不是具体细节。', options: agreeOptions },
  { id: 205, assessmentId: 2, text: '团队讨论中，我愿意先表达观点带动节奏。', options: agreeOptions },
  { id: 206, assessmentId: 2, text: '压力下，我会先梳理事实和优先级再回应。', options: agreeOptions },
  { id: 301, assessmentId: 3, text: '遇到分歧时，我会直接指出关键问题并推动决策。', options: agreeOptions },
  { id: 302, assessmentId: 3, text: '我擅长用热情和表达感染团队气氛。', options: agreeOptions },
  { id: 303, assessmentId: 3, text: '我喜欢稳定节奏，重视团队关系的持续信任。', options: agreeOptions },
  { id: 304, assessmentId: 3, text: '提交成果前，我会反复核对细节和标准。', options: agreeOptions },
  { id: 305, assessmentId: 3, text: '任务目标不清晰时，我会主动要求明确边界和负责人。', options: agreeOptions },
  { id: 306, assessmentId: 3, text: '在协作中，我更愿意倾听并协调不同成员的节奏。', options: agreeOptions },
  { id: 401, assessmentId: 4, text: '面对新知识，我能快速建立学习路径并验证效果。', options: agreeOptions },
  { id: 402, assessmentId: 4, text: '任务推进受阻时，我会主动拆解风险并寻找替代方案。', options: agreeOptions },
  { id: 403, assessmentId: 4, text: '我能把复杂信息整理成他人容易理解的结构。', options: agreeOptions },
  { id: 404, assessmentId: 4, text: '我习惯用复盘记录持续优化自己的工作方式。', options: agreeOptions },
  { id: 405, assessmentId: 4, text: '跨团队协作时，我会提前同步目标、依赖和时间点。', options: agreeOptions },
  { id: 406, assessmentId: 4, text: '即使目标模糊，我也能先产出可讨论的阶段成果。', options: agreeOptions }
]

export const assessmentResults: AssessmentResult[] = [
  {
    assessmentId: 1,
    title: '研究型 + 社会型',
    summary: '你擅长分析问题，也愿意帮助他人成长，适合兼具逻辑推理和沟通协作的岗位。',
    scores: [{ name: '研究型', score: 92 }, { name: '社会型', score: 84 }, { name: '企业型', score: 78 }, { name: '艺术型', score: 66 }, { name: '常规型', score: 58 }, { name: '现实型', score: 42 }],
    careers: [
      { title: '产品经理', reason: '需要用户理解、需求拆解和跨团队推动。', match: 91 },
      { title: '数据分析师', reason: '匹配你的结构化分析与业务洞察能力。', match: 87 },
      { title: '用户研究员', reason: '适合将访谈、数据和产品改进连接起来。', match: 84 }
    ]
  },
  {
    assessmentId: 2,
    title: 'ENFJ 倾向',
    summary: '你偏向主动连接他人，并能在目标和团队氛围之间取得平衡。',
    scores: [{ name: '外向', score: 86 }, { name: '直觉', score: 78 }, { name: '情感', score: 82 }, { name: '判断', score: 74 }],
    careers: [
      { title: '产品经理', reason: '适合在用户、业务和研发之间建立共识。', match: 90 },
      { title: '运营策划', reason: '需要持续沟通、内容组织和结果复盘。', match: 84 },
      { title: '人才发展专员', reason: '匹配你的沟通引导和成长支持偏好。', match: 80 }
    ]
  },
  {
    assessmentId: 3,
    title: '影响型 + 稳健型',
    summary: '你擅长调动协作氛围，同时重视关系稳定和执行节奏。',
    scores: [{ name: '支配型 D', score: 72 }, { name: '影响型 I', score: 88 }, { name: '稳健型 S', score: 81 }, { name: '谨慎型 C', score: 69 }],
    careers: [
      { title: '客户成功经理', reason: '需要沟通影响、关系维护和问题推进。', match: 88 },
      { title: '项目协调专员', reason: '适合处理多方协作和节奏管理。', match: 83 },
      { title: '产品运营', reason: '兼顾表达、反馈收集和跨团队落地。', match: 81 }
    ]
  },
  {
    assessmentId: 4,
    title: '学习力 + 执行力突出',
    summary: '你能较快吸收新信息，并将目标拆解为可执行的阶段任务。',
    scores: [{ name: '学习力', score: 89 }, { name: '执行力', score: 86 }, { name: '沟通力', score: 78 }, { name: '复盘力', score: 82 }],
    careers: [
      { title: '前端工程师', reason: '适合持续学习技术栈并在项目中稳定交付。', match: 88 },
      { title: '项目助理', reason: '匹配任务拆解、节奏跟进和跨方同步能力。', match: 82 },
      { title: '数据运营', reason: '需要结构化分析、快速学习和持续复盘。', match: 79 }
    ]
  }
]

export const assessmentResult: AssessmentResult = assessmentResults[0]

export function getAssessmentQuestions(assessmentId: number) {
  return assessmentQuestions.filter((question) => question.assessmentId === assessmentId)
}

export function getAssessmentResult(assessmentId: number) {
  return assessmentResults.find((result) => result.assessmentId === assessmentId) ?? assessmentResult
}

export const resumeHistory: ResumeRecord[] = [
  { id: 1, fileName: '林晨-前端工程师-2026.pdf', uploadTime: '2026-05-22 14:20', score: 86, status: '已分析', version: 'V4' },
  { id: 2, fileName: '林晨-产品实习-2026.pdf', uploadTime: '2026-05-10 18:42', score: 78, status: '需优化', version: 'V3' },
  { id: 3, fileName: '林晨-校园招聘.pdf', uploadTime: '2026-04-28 09:16', score: 72, status: '已分析', version: 'V2' }
]

export const resumeAnalysis: ResumeAnalysis = {
  score: 86,
  dimensions: [
    { name: '岗位匹配', score: 88, comment: '前端技术栈完整，项目经历与目标岗位一致。' },
    { name: '成果量化', score: 76, comment: '部分项目缺少性能、增长或效率数据。' },
    { name: '表达清晰', score: 91, comment: '结构清晰，关键词覆盖较好。' },
    { name: '版式规范', score: 84, comment: '信息密度合适，建议精简重复技能描述。' }
  ],
  issues: ['项目经历中“负责”“参与”表述较多，个人贡献边界不够明确。', '缺少与目标岗位 JD 直接对应的关键词，如性能监控、工程化治理。', '教育经历和获奖信息占比略高。'],
  suggestions: ['为每个核心项目补充 1 个可验证指标。', '将技能栈按熟练度和业务应用场景分组。', '针对不同岗位维护互联网、企业服务两版简历。']
}

export const interviewHistory: InterviewSession[] = [
  { id: 1, role: '前端工程师', company: '星云科技', time: '2026-05-24 20:00', score: 84, status: '已完成' },
  { id: 2, role: '产品经理', company: '云帆智能', time: '2026-05-18 19:30', score: 79, status: '已完成' },
  { id: 3, role: '前端实习生', company: '北辰网络', time: '2026-05-12 21:00', score: 88, status: '已完成' }
]

export const interviewFeedback: InterviewFeedback = {
  overall: 84,
  dimensions: [{ name: '技术深度', score: 82 }, { name: '表达结构', score: 88 }, { name: '岗位理解', score: 80 }, { name: '追问应对', score: 85 }],
  questions: [
    { id: 1, question: '请介绍一个你主导的前端项目。', answer: '围绕低代码配置台讲述了背景、方案和结果。', feedback: '结构完整，建议补充技术选型取舍。', score: 86 },
    { id: 2, question: '如何定位页面白屏问题？', answer: '从网络、资源、运行时错误和监控链路展开。', feedback: '思路清晰，可加入真实工具链示例。', score: 83 }
  ],
  suggestions: ['准备 2 个可深挖项目，覆盖架构、性能和协作冲突。', '回答技术问题时先给排查框架，再展开关键路径。', '结尾主动反问团队工程规范和成长机制。']
}

export const jobs: Job[] = [
  { id: 1, title: '前端工程师', company: '星云科技', city: '杭州', salary: '18-28K', match: 94, tags: ['Vue 3', 'TypeScript', '可视化'], description: '负责智能招聘平台 Web 端研发，建设组件库和数据看板。', requirements: ['熟悉 Vue 3 与 TypeScript', '具备工程化和性能优化经验', '能与产品、设计高效协作'], highlights: ['导师制完善', '技术分享活跃', '核心业务团队'], gaps: ['补充大规模表单性能优化案例', '加强 E2E 测试经验'] },
  { id: 2, title: '产品经理助理', company: '云帆智能', city: '上海', salary: '12-18K', match: 87, tags: ['B 端产品', '数据分析', 'AI'], description: '参与职业规划 Agent 产品设计，跟进需求、原型和数据分析。', requirements: ['有产品实习或项目经验', '理解 AI 应用流程', '具备良好文档能力'], highlights: ['AI 原生产品', '成长路径清晰'], gaps: ['补充用户访谈作品集', '沉淀指标体系案例'] },
  { id: 3, title: '用户增长分析师', company: '青藤教育', city: '北京', salary: '15-22K', match: 82, tags: ['SQL', 'A/B Test', '增长'], description: '负责学生端增长漏斗分析和策略实验。', requirements: ['掌握 SQL 和数据可视化', '能独立设计实验', '业务敏感度强'], highlights: ['数据驱动文化', '跨部门资源充足'], gaps: ['强化 SQL 作品', '补充增长实验方法论'] }
]

export const courses: Course[] = [
  { id: 1, title: 'Vue 3 企业级工程实践', provider: '极客时间', progress: 68, level: '进阶' },
  { id: 2, title: 'TypeScript 类型建模', provider: '慕课网', progress: 42, level: '中级' },
  { id: 3, title: '前端性能监控与优化', provider: '掘金小册', progress: 25, level: '进阶' }
]

export const milestones: LearningMilestone[] = [
  { id: 1, title: '完成 Vue 3 基础复盘', date: '05-12', status: 'finish' },
  { id: 2, title: '补齐 TypeScript 泛型训练', date: '05-28', status: 'process' },
  { id: 3, title: '完成性能优化项目复盘', date: '06-10', status: 'wait' },
  { id: 4, title: '准备前端系统设计面试', date: '06-22', status: 'wait' }
]
