# 就业辅导 Agent 系统 - 开发进度

## 项目路径
/root/projects/career-agent

## 设计文档
DESIGN_DOC.md — 完整设计文档（2959行）

## 已完成工作

### P1: 打通路由和接口路径 ✅ 已完成
- Gateway 路由：Nacos 配置中已添加 /agent/**, /kb/**, /model/**, /chat/**, /profile/**, /resume/** 路由
- Controller 路径对齐：ChatSessionController 增加 /chat/session/* 兼容路径
- AdminModelController 增加 /model/config/* 兼容路径
- 前端 API 代理：career-web/vite.config.ts 添加 rewrite 规则（/api → 去掉前缀）
- 后端编译通过、前端构建通过

### P1: 部署闭环（进行中）
- Docker Compose 补充 agent/model/knowledge/career-web 服务
- Dockerfile 创建
- .env.example 创建
- SQL 初始化完善
- agent_definition 表补充

## 技术栈
- 后端：RuoYi-Cloud + Spring Cloud Alibaba + MyBatis-Plus
- 前端：Vue3 + Vite + Element Plus + Pinia + TypeScript（career-web/）
- 模型：gpt-5.5 via sharedchat proxy (CODEX_API_KEY)
- Codex CLI v0.134.0, provider=codex, sandbox=workspace-write

## 关键注意事项
- 所有代码变更必须通过 Codex CLI 执行
- Codex 运行命令：export 三个环境变量后 codex exec -m gpt-5.5 --sandbox workspace-write
- 后端编译：mvn -pl ruoyi-modules/ruoyi-agent,ruoyi-modules/ruoyi-model -am -DskipTests compile
- 前端编译：cd career-web && npm run build
- git 提交身份：chronoir1110specials-spec <chronoir1110specials@gmail.com>
