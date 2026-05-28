# 阶段2：Agent 核心能力

**目标**: 构建 Agent Runtime 核心组件：用户画像、对话记忆、动态 Prompt、结构化输出。

**依赖**: 阶段1 全部完成
**设计文档参考**: 第8章（Agent 设计）、§8.6-8.8

---

## 任务 2.1: 用户画像 CRUD

**目标**: 实现用户画像的增删改查，支持学生用户维护个人求职信息。

**输入**: 设计文档 §5.1.2 + §10.2
**输出**:
- ruoyi-agent/src/main/java/.../entity/UserProfile.java
- ruoyi-agent/src/main/java/.../mapper/UserProfileMapper.java
- ruoyi-agent/src/main/java/.../service/UserProfileService.java
- ruoyi-agent/src/main/java/.../controller/ProfileController.java

**验收标准**:
1. 实体字段: school, major, grade, targetPosition, targetCity, skillTags, projectTags, jobStage
2. user_id 关联 sys_user
3. 接口: GET /profile/get, POST /profile/save, PUT /profile/update
4. 用户只能操作自己的画像

---

## 任务 2.2: 对话记忆管理（短期 + 长期）

**目标**: 实现会话消息存储和上下文窗口裁剪。

**输入**: 设计文档 §8.6
**输出**:
- ruoyi-agent/src/main/java/.../service/ContextWindowManager.java
- ruoyi-agent/src/main/java/.../service/ConversationMemoryService.java

**验收标准**:
1. ContextWindowManager 按"最近 N 轮 + Token 预算"裁剪上下文
2. 裁剪时从最旧消息开始移除
3. 超过 N 轮的消息生成摘要存回 chat_session.context_summary
4. 不同 Agent 类型设置不同预算（简历优化 vs 对话）
5. Redis 缓存最近 N 轮（可选，先 MySQL 实现）

---

## 任务 2.3: Prompt 模板管理

**目标**: 实现 Agent Prompt 模板的基础管理。

**输入**: 设计文档 §8.7 动态 Prompt
**输出**:
- ruoyi-agent/src/main/java/.../entity/PromptTemplate.java
- ruoyi-agent/src/main/java/.../service/PromptTemplateService.java
- 预置 6 个 Agent 的基础 Prompt 常量

**验收标准**:
1. 每个 Agent 类型有一个默认 Prompt 模板
2. 支持变量替换: {{userProfile}}, {{jobTarget}}, {{conversationSummary}}
3. 模板从数据库读取（毕设阶段也可先用枚举常量）
4. 至少包含: 简历优化、岗位分析、模拟面试、知识库问答、求职材料、通用对话

---

## 任务 2.4: 结构化输出解析器

**目标**: 实现 JSON 输出解析，支持简历分析、岗位分析的结构化结果。

**输入**: 设计文档 §8.8 结构化输出设计
**输出**:
- ruoyi-agent/src/main/java/.../parser/OutputParser.java (接口)
- ruoyi-agent/src/main/java/.../parser/JsonOutputParser.java
- ruoyi-agent/src/main/java/.../parser/MarkdownOutputParser.java

**验收标准**:
1. JsonOutputParser 解析模型返回的 JSON，校验 Schema
2. 解析失败时返回默认错误结构（不抛异常）
3. MarkdownOutputParser 保留原始 markdown（用于对话展示）
4. 支持 resume_optimize 和 job_analyze 的输出 Schema（§8.8.1, §8.8.2）

---

## 任务 2.5: Agent 类型枚举 + 定义注册

**目标**: 定义 6 个核心 Agent 的属性配置。

**输入**: 设计文档 §8.2 + §8.11
**输出**:
- ruoyi-agent/src/main/java/.../enums/AgentType.java
- ruoyi-agent/src/main/java/.../config/AgentDefinitionRegistry.java

**验收标准**:
1. AgentType 枚举: RESUME_OPTIMIZE, JOB_ANALYZE, INTERVIEW, CAREER_PLAN, KB_QA, MATERIAL_GEN, GENERAL_CHAT
2. AgentDefinitionRegistry 管理每个 Agent 的: allowedTools, memoryPolicy, ragPolicy, maxTurns
3. 毕设阶段先硬编码，不放到数据库

---

## 任务 2.6: AgentOrchestrator 编排器

**目标**: 实现 Agent 请求的统一编排入口。

**输入**: 设计文档 §8.10 Agent Runtime
**输出**:
- ruoyi-agent/src/main/java/.../service/AgentOrchestrator.java

**验收标准**:
1. 根据 AgentType 选择对应的 Prompt 模板
2. 注入用户画像和历史摘要
3. 调用 ChatModelRouter
4. 解析输出（结构化或 Markdown）
5. 保存会话和日志
6. 统一返回 AgentResponse（包含 content + metadata）

---

## 任务 2.7: Agent 任务日志 + 步骤日志

**目标**: 实现 Agent 执行过程的审计记录。

**输入**: 设计文档 §10.11 + §10.12
**输出**:
- ruoyi-agent/src/main/java/.../entity/AgentTask.java
- ruoyi-agent/src/main/java/.../entity/AgentStepLog.java
- ruoyi-agent/src/main/java/.../mapper/ 对应 Mapper
- ruoyi-agent/src/main/java/.../service/AgentTaskLogger.java

**验收标准**:
1. 每次 Agent 执行创建 agent_task 记录
2. 模型调用、工具调用、RAG 检索各记录 agent_step_log
3. 日志只记录摘要和 Token，不记录敏感全文
4. task 状态流转: PENDING → RUNNING → SUCCEEDED/FAILED/TIMEOUT

---

## 任务 2.8: 统一 Agent 接口改造

**目标**: 将 ChatController 改造为 AgentController，支持 Agent 类型选择。

**输入**: 设计文档 §11.4
**输出**: ruoyi-agent/src/main/java/.../controller/AgentController.java

**验收标准**:
1. POST /agent/ask 接受 agentType 参数，走 AgentOrchestrator
2. 保留原 /chat/message/send 兼容简单对话
3. Agent 接口返回 agentType 和结构化标记字段
4. 响应统一用 R<AgentResponse> 包装
