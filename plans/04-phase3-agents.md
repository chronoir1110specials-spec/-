# 阶段3：核心就业 Agent

**目标**: 实现简历优化、岗位分析两个核心 Agent，打通完整链路。

**依赖**: 阶段2 全部完成
**设计文档参考**: §5.1.4、§5.1.5、§8.2-8.8

---

## 任务 3.1: 简历信息实体 + 文件上传

**目标**: 实现简历上传和文本解析功能。

**输入**: 设计文档 §5.1.4 + §10.5
**输出**:
- ruoyi-agent/src/main/java/.../entity/ResumeInfo.java
- ruoyi-agent/src/main/java/.../mapper/ResumeInfoMapper.java
- ruoyi-agent/src/main/java/.../controller/ResumeController.java
- ruoyi-agent/src/main/java/.../service/ResumeService.java

**验收标准**:
1. POST /resume/upload 上传 PDF/DOCX/TXT 文件
2. 文件校验: 扩展名白名单、大小 ≤10MB、MIME 类型
3. PDF 用 PDFBox 解析、DOCX 用 POI 解析
4. 解析后的纯文本存入 resume_info.content
5. parse_status 记录解析状态
6. 用户隔离：只能访问自己的简历

**Codex Prompt 要点**:
- Maven 添加 pdfbox 和 poi-ooxml 依赖
- 上传文件存储路径: `/data/uploads/resumes/{userId}/`
- 解析失败返回友好提示

---

## 任务 3.2: 简历优化 Agent Prompt 配置

**目标**: 编写简历优化的专用 Prompt 模板。

**输入**: 设计文档 §8.7
**输出**: ruoyi-agent/src/main/java/.../prompt/ResumeOptimizePrompts.java

**验收标准**:
1. 包含 System Prompt（角色定位 + 输出格式要求）
2. 输出 Schema 与 §8.8.1 一致: score, summary, problems[], suggestions[], optimizedText, keywords[]
3. Prompt 要求模型以 JSON 格式返回
4. 注入用户画像: 目标岗位、技能标签
5. Prompt 中要求使用 STAR 法则评估项目经历

---

## 任务 3.3: 简历优化 Agent 核心逻辑

**目标**: 实现完整的简历分析流程。

**输入**: 设计文档 §5.1.4 简历优化 Agent
**输出**:
- ruoyi-agent/src/main/java/.../service/ResumeOptimizeService.java
- ruoyi-agent/src/main/java/.../agent/ResumeOptimizeAgent.java

**验收标准**:
1. 流程: 获取简历文本 → 加载画像 → 构建 Prompt → 调用模型 → 解析 JSON → 保存分析结果
2. 解析结果存入 resume_info.analysis_result + score
3. 返回结构化数据给前端（前后端分离用的 JSON）
4. Agent 任务日志正确记录
5. 模型调用日志正确记录

---

## 任务 3.4: 简历优化接口 + 端到端验证

**目标**: 暴露简历优化 API 并完成验证。

**输入**: 任务 3.3 完成
**输出**: ruoyi-agent/src/main/java/.../controller/ResumeOptimizeController.java

**验收标准**:
1. POST /agent/resume/optimize 接受 resumeId 参数
2. 返回结构化 JSON（score, summary, problems, suggestions...）
3. 未上传简历时返回 400 + 错误信息
4. 用 curl 发送完整请求，验证返回 JSON 格式正确

---

## 任务 3.5: 岗位信息实体 + JD 输入

**目标**: 实现岗位 JD 的录入存储。

**输入**: 设计文档 §10.6
**输出**:
- ruoyi-agent/src/main/java/.../entity/JobInfo.java
- ruoyi-agent/src/main/java/.../mapper/JobInfoMapper.java
- ruoyi-agent/src/main/java/.../controller/JobController.java

**验收标准**:
1. POST /job/analyze 接受 jobDescription 文本（也可支持粘贴）
2. 前端可传 jobName, companyName, jobDescription
3. 保存 JD 到 job_info 表
4. 用户隔离

---

## 任务 3.6: 岗位分析 Agent Prompt 配置

**目标**: 编写岗位分析的专用 Prompt 模板。

**输入**: 设计文档 §5.1.5
**输出**: ruoyi-agent/src/main/java/.../prompt/JobAnalyzePrompts.java

**验收标准**:
1. System Prompt 包含角色定位
2. 输出 Schema 与 §8.8.2 一致: jobTitle, requiredSkills[], bonusSkills[], matchScore, resumeAdvice, interviewTopics[]
3. 要求模型提取核心职责、必备技能、加分技能
4. 注入用户画像进行匹配分析

---

## 任务 3.7: 岗位分析 Agent 核心逻辑

**目标**: 实现岗位分析流程。

**输入**: 设计文档 §5.1.5
**输出**:
- ruoyi-agent/src/main/java/.../service/JobAnalyzeService.java
- ruoyi-agent/src/main/java/.../agent/JobAnalyzeAgent.java

**验收标准**:
1. 流程: 获取 JD → 加载画像 → 构建 Prompt → 调用模型 → 解析 JSON → 保存
2. 保存到 job_info.analysis_result + match_score
3. 返回结构化 JSON

---

## 任务 3.8: 岗位分析接口 + 验证

**目标**: 暴露岗位分析 API 并验证。

**输入**: 任务 3.7 完成
**输出**: ruoyi-agent/src/main/java/.../controller/JobAnalyzeController.java

**验收标准**:
1. POST /agent/job/analyze 接受 jobId 参数
2. 返回结构化 JSON
3. curl 验证通过

---

## 任务 3.9: 求职材料生成 Agent

**目标**: 实现自我介绍、求职邮件等快速材料生成。

**输入**: 设计文档 §5.1.8
**输出**:
- ruoyi-agent/src/main/java/.../agent/MaterialGenerateAgent.java
- ruoyi-agent/src/main/java/.../controller/MaterialController.java

**验收标准**:
1. POST /agent/material/generate 接受 type 参数（self_intro, job_email, thank_letter, self_summary）
2. 走 SingleCall 模式（一次模型调用）
3. 返回 Markdown 格式文本
4. 注入用户画像

---

## 任务 3.10: 对话 Agent 改造（通用 + Agent 选择）

**目标**: 改造对话 Controller 支持前端选择 Agent 类型。

**输入**: 已有 ChatController
**输出**: 优化后的 AgentController

**验收标准**:
1. POST /agent/ask 接受 agentType 参数
2. 根据 agentType 路由到对应的 Agent 处理
3. 无 agentType 时走通用对话
4. 前端可选的 Agent 列表: GET /agent/list

---

## 任务 3.11: SSE 流式输出

**目标**: 改造对话接口支持 SSE 流式返回。

**输入**: 设计文档 §11.3 SSE 流式输出
**输出**: ruoyi-agent/src/main/java/.../controller/StreamController.java

**验收标准**:
1. POST /chat/message/stream 使用 text/event-stream
2. 事件类型: start, delta, done, error
3. 模型调用使用流式模式
4. 流结束后保存完整消息
5. curl 测试流式输出正常

**注意**: 此任务依赖 DeepSeekClient 和 GLM51Client 支持 stream=true

---

## 任务 3.12: 模型调用日志统计接口

**目标**: 实现每日调用次数查询和 Token 统计。

**输入**: 设计文档 §7.6 成本控制
**输出**: ruoyi-agent/src/main/java/.../controller/ModelStatsController.java

**验收标准**:
1. GET /model/stats/today 查询当前用户今日调用次数
2. GET /model/stats/monthly 查询本月统计
3. 前端可在首页仪表盘展示
