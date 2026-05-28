# 阶段5：后台管理 + 日志

**目标**: 完善管理后台的模型配置、调用日志、Agent 任务日志功能。

**依赖**: 阶段3 基本完成
**设计文档参考**: §5.1.11、§5.1.12、§11.6、§11.7

---

## 任务 5.1: 模型配置管理接口

**目标**: 管理员可查看和修改模型配置（DeepSeek/GLM/Embedding）。

**输入**: 设计文档 §10.9 + §11.6
**输出**: ruoyi-model/src/main/java/.../controller/ModelConfigController.java

**验收标准**:
1. GET /model/config/list 查询所有模型配置（api_key 脱敏）
2. POST /model/config/save 保存模型配置
3. api_key 写入时加密存储（AES）
4. 管理员权限校验

---

## 任务 5.2: 模型连接测试接口

**目标**: 管理员可在后台测试模型连通性。

**输入**: 设计文档 §11.6
**输出**: ruoyi-model/src/main/java/.../controller/ModelTestController.java

**验收标准**:
1. POST /model/test/primary 发送简单消息测试 DeepSeek
2. POST /model/test/fallback 发送简单消息测试 GLM
3. 返回耗时、Token 消耗、是否成功

---

## 任务 5.3: 模型调用日志查询

**目标**: 管理员可查看模型调用历史。

**输入**: 设计文档 §10.10
**输出**: ruoyi-model/src/main/java/.../controller/ModelLogController.java

**验收标准**:
1. GET /model/log/list 分页查询
2. 支持按用户、模型、日期范围、状态筛选
3. 显示: 用户、模型、Token、耗时、状态、是否兜底
4. 不显示完整 Prompt 内容

---

## 任务 5.4: Agent 任务日志查询

**目标**: 管理员可查看 Agent 任务执行历史和步骤详情。

**输入**: 设计文档 §10.11 + §10.12
**输出**: ruoyi-agent/src/main/java/.../controller/AgentTaskLogController.java

**验收标准**:
1. GET /admin/log/agent 分页查询 Agent 任务列表
2. 支持按 Agent 类型、状态、用户筛选
3. 点击任务查看步骤列表
4. 步骤日志显示: 步骤类型、工具名、状态、Token、耗时
5. 不显示完整 Prompt 和输入输出全文

---

## 任务 5.5: 系统统计仪表盘

**目标**: 管理员首页展示系统核心数据。

**输入**: 设计文档 §16.3
**输出**: ruoyi-system 或 ruoyi-agent 的仪表盘接口

**验收标准**:
1. GET /admin/dashboard 返回:
   - 用户总数
   - 今日会话数
   - 今日模型调用次数
   - 本月 Token 消耗
   - DeepSeek vs GLM 调用比例
   - 知识库文档数
   - 平均响应时间
2. 缓存统计数据（Redis），定期刷新

---

## 任务 5.6: 用户管理 + 权限检查

**目标**: 确保所有接口有正确的权限控制。

**输入**: 若依现有权限体系
**输出**: 权限校验确认

**验收标准**:
1. 普通学生用户不能访问管理后台接口
2. 用户只能查看自己的会话/简历/分析记录
3. 管理员可查看所有用户的日志
4. 所有涉及敏感数据的接口做对象级权限校验
5. 问题修复: 检查所有 Controller 是否有 `@PreAuthorize`

**Codex Prompt 要点**:
- 逐一检查所有新增 Controller
- 复用若依的 `@PreAuthorize` 注解
- 确保 `userId` 从认证信息中获取，不接受前端传入
