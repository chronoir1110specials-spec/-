# 阶段1：模型适配层 + 基础对话

**目标**: 构建统一模型适配层，接通 DeepSeek V4 Pro 和 GLM 5.1，实现基础对话。

**依赖**: 阶段0 全部完成
**设计文档参考**: 第7章（模型调用方案）、第8章（Agent 设计）

---

## 任务 1.1: 创建 model_config 实体 + Mapper + Service

**目标**: 实现模型配置表的基础 CRUD。

**输入**: 设计文档 §10.9 model_config 表结构
**输出**: 
- ruoyi-model/src/main/java/.../entity/ModelConfig.java
- ruoyi-model/src/main/java/.../mapper/ModelConfigMapper.java
- ruoyi-model/src/main/java/.../service/IModelConfigService.java
- ruoyi-model/src/main/java/.../service/impl/ModelConfigServiceImpl.java

**验收标准**:
1. 实体类字段与 §10.9 一致
2. 使用 MyBatis-Plus 的 BaseMapper
3. Service 至少提供 queryPrimaryModel() 和 queryFallbackModel() 方法
4. api_key 字段使用 @TableField 标记为敏感字段

---

## 任务 1.2: 创建 ChatRequest / ChatResponse 通用模型

**目标**: 定义统一的聊天请求响应结构，隔离底层 API 差异。

**输入**: 设计文档 §7.3 统一接口设计
**输出**: 
- ruoyi-model/src/main/java/.../dto/ChatRequest.java
- ruoyi-model/src/main/java/.../dto/ChatResponse.java
- ruoyi-model/src/main/java/.../dto/ChatMessage.java

**验收标准**:
1. ChatRequest 包含: model, messages (List<ChatMessage>), temperature, maxTokens
2. ChatResponse 包含: content, model, promptTokens, completionTokens, totalTokens, finishReason, success, errorMessage
3. ChatMessage 包含: role (system/user/assistant), content
4. 所有字段与设计文档 §7.3 的设计一致

---

## 任务 1.3: 实现 ChatModelClient 接口 + DeepSeek 实现

**目标**: 实现统一模型调用接口及 DeepSeek V4 Pro 客户端。

**输入**: 设计文档 §7.3 接口设计和 DO API 格式
**输出**: 
- ruoyi-model/src/main/java/.../client/ChatModelClient.java (接口)
- ruoyi-model/src/main/java/.../client/impl/DeepSeekClient.java

**验收标准**:
1. ChatModelClient 接口定义 chat(ChatRequest) 方法
2. DeepSeekClient 通过 HTTP 调用 DO Inference API
3. 使用 Java HttpClient 或 RestTemplate，不用第三方 SDK
4. API Key 和 Base URL 从 model_config 表动态读取
5. 支持超时设置（60秒）
6. 返回结构化 ChatResponse

**Codex Prompt 要点**:
- API 地址: `https://api.deepseek.com/v1/chat/completions`（或 DigitalOcean 实际 endpoint）
- 模型名: deepseek-v4-pro
- HTTP 调用需设置合理的超时和错误处理

---

## 任务 1.4: 实现 GLM 5.1 兜底客户端

**目标**: 实现 GLM 5.1 客户端，作为兜底模型。

**输入**: 设计文档 §7.3 
**输出**: ruoyi-model/src/main/java/.../client/impl/GLM51Client.java

**验收标准**:
1. 实现 ChatModelClient 接口
2. 通过 HTTP 调用 GLM API
3. 响应格式适配为 ChatResponse
4. 错误处理完整（超时、空响应、格式异常）

---

## 任务 1.5: 实现 ChatModelRouter 模型路由器

**目标**: 实现主/兜底模型自动切换逻辑。

**输入**: 设计文档 §7.4 模型路由策略
**输出**: ruoyi-model/src/main/java/.../router/ChatModelRouter.java

**验收标准**:
1. 优先调用 DeepSeek V4 Pro
2. 触发兜底条件: 超时、5xx、限流、空响应、异常
3. 兜底调用 GLM 5.1
4. 每次调用记录: isFallback, 错误信息
5. 支持配置开关 `enable-fallback`
6. 失败时抛出明确业务异常，不直接返回 null

---

## 任务 1.6: 实现模型调用日志记录

**目标**: 实现 model_call_log 写入，记录每次模型调用。

**输入**: 设计文档 §10.10 表结构 + §8.9 拦截器设计
**输出**:
- ruoyi-model/src/main/java/.../entity/ModelCallLog.java
- ruoyi-model/src/main/java/.../mapper/ModelCallLogMapper.java
- ruoyi-model/src/main/java/.../service/ModelCallLogService.java
- ruoyi-model/src/main/java/.../interceptor/ModelLogInterceptor.java

**验收标准**:
1. 实体类字段与 §10.10 完全一致
2. ModelLogInterceptor 在每次模型调用后自动写入日志
3. 日志包含: userId, sessionId, provider, modelName, isFallback, tokens, costTime, status, errorMessage
4. 异步写入，不阻塞主流程

---

## 任务 1.7: 实现基础对话 Controller

**目标**: 在 ruoyi-agent 中实现基础对话接口。

**输入**: 设计文档 §11.3 对话接口设计
**输出**:
- ruoyi-agent/src/main/java/.../controller/ChatController.java
- ruoyi-agent/src/main/java/.../service/ChatService.java
- ruoyi-agent/src/main/java/.../entity/ChatSession.java
- ruoyi-agent/src/main/java/.../entity/ChatMessage.java
- ruoyi-agent/src/main/java/.../mapper/ChatSessionMapper.java
- ruoyi-agent/src/main/java/.../mapper/ChatMessageMapper.java

**验收标准**:
1. `POST /chat/session/create` 创建会话，自动生成标题
2. `GET /chat/session/list` 查询用户会话列表
3. `POST /chat/message/send` 发送消息，调用模型返回结果
4. 消息和会话保存到数据库
5. 用户只能看到自己的会话
6. 基础鉴权（使用 Gateway 传递的 JWT token）

---

## 任务 1.8: 端到端连通验证

**目标**: 确保从 Controller → Router → Client → Model API 全链路可用。

**输入**: 以上所有代码
**输出**: 可用的聊天接口

**验证步骤**:
1. 启动 ruoyi-gateway + ruoyi-auth + ruoyi-agent 服务
2. 通过 Gateway 调用 `/chat/message/send` 发送测试消息
3. 验证模型返回内容
4. 手动触发 DeepSeek 失败，验证 GLM 5.1 兜底
5. 验证 model_call_log 记录正确

**注意**: 此阶段不需要前端，用 curl 或 Postman 验证即可
