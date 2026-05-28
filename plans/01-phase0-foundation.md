# 阶段0：若依脚手架初始化

**目标**: 基于 RuoYi-Cloud 搭建主体工程，创建新增模块骨架，完成环境配置。

## 任务 0.1: 克隆 RuoYi-Cloud 并初始化

**目标**: 获取若依微服务脚手架，建立基础工程结构。

**输入**: 无
**输出**: 可编译运行的若依基础工程
**涉及文件**: 整个项目目录
**验收标准**:
1. `git clone` RuoYi-Cloud 到 `/root/projects/career-agent/ruoyi-cloud`
2. Maven 下载所有依赖成功
3. `mvn clean install -DskipTests` 全模块编译通过
4. Git 初始提交完成

**Codex Prompt 要点**:
- 克隆若依官方仓库（gitee 或 github）
- 建立项目根目录 README.md
- 首次编译验证
- 设计文档 `/root/projects/career-agent/docs/design.md` 必须先阅读

---

## 任务 0.2: 新增 ruoyi-agent、ruoyi-knowledge、ruoyi-model 模块

**目标**: 创建三个新增微服务模块的 Maven 骨架。

**输入**: 若依工程的 pom.xml 结构
**输出**: 三个模块的 Maven 骨架，含基础目录结构
**涉及文件**: 
- 根 pom.xml（添加模块声明）
- ruoyi-agent/pom.xml
- ruoyi-knowledge/pom.xml
- ruoyi-model/pom.xml
- 各模块的 Application.java 启动类
- 各模块的 bootstrap.yml

**验收标准**:
1. 三个模块在根 pom.xml 的 `<modules>` 中声明
2. 每个模块有独立的 pom.xml，继承/common 依赖
3. 每个模块有独立 Application 启动类
4. 每个模块有 bootstrap.yml 配置 Nacos 注册
5. `mvn clean compile` 通过

**Codex Prompt 要点**:
- 参照 ruoyi-system 的模块结构
- 不需要 controller/service，先只要骨架
- ruoyi-agent 作为核心模块，后续逐步添加功能
- 设计文档 §6.3 和 §6.4 微服务划分方案必须遵循

---

## 任务 0.3: 配置 Nacos + MySQL + Redis 连接

**目标**: 配置所有模块连接 Nacos 注册中心和数据库。

**输入**: 若依各模块的默认配置
**输出**: 所有模块的 bootstrap.yml 和 application.yml 可正确连接基础设施
**涉及文件**: 
- ruoyi-gateway/src/main/resources/bootstrap.yml
- ruoyi-auth/src/main/resources/bootstrap.yml
- ruoyi-system/src/main/resources/bootstrap.yml
- ruoyi-agent/src/main/resources/bootstrap.yml
- ruoyi-knowledge/src/main/resources/bootstrap.yml
- ruoyi-model/src/main/resources/bootstrap.yml

**验收标准**:
1. Nacos 地址指向 `127.0.0.1:8848`（本地开发）
2. MySQL 连接配置正确
3. Redis 连接配置正确
4. 所有模块配置一致

**Codex Prompt 要点**:
- 复用若依默认配置格式
- 敏感信息用占位符（实际值在本地配置）
- 设计文档 §7.5 模型配置方案参考

---

## 任务 0.4: 创建数据库初始化 SQL

**目标**: 创建建库建表 SQL，从设计文档的数据库设计（第10章）生成 DDL。

**输入**: 设计文档第10章
**输出**: init.sql 包含所有核心表
**涉及文件**: sql/init.sql

**验收标准**:
1. 包含若依基础表（复用若依原 SQL）
2. 包含新增表：user_profile, chat_session, chat_message, resume_info, job_info, kb_document, kb_chunk, model_config, model_call_log, agent_task, agent_step_log
3. 表结构与设计文档 §10 一致
4. 字符集 utf8mb4
5. 索引合理

**Codex Prompt 要点**:
- 先读设计文档第10章
- 若依原 SQL 为基础，追加新增表
- 每个表要有 PRIMARY KEY 和必要索引
- chat_message 的 role 字段参考设计 §10.4

---

## 任务 0.5: 创建若依前端项目 + 学生端前端目录

**目标**: 克隆若依前端，创建学生端前端项目骨架。

**输入**: 若依前端仓库地址
**输出**: 两个前端项目目录
**涉及文件**: ruoyi-ui/, ai-web/

**验收标准**:
1. ruoyi-ui 从若依前端仓库克隆，可正常 `npm install && npm run dev`
2. ai-web/ 创建 Vue3 + Vite + Element Plus 项目骨架
3. ai-web 可正常 `npm install && npm run dev`

**Codex Prompt 要点**:
- 若依前端用于管理后台
- ai-web 用于学生端（全新 Vue3 项目）
- 设计文档 §12 前端页面设计参考

---

## 任务 0.6: 配置开发环境 + 验证全链路编译

**目标**: 确保所有模块可编译，前端可启动。

**输入**: 项目完整代码
**输出**: 编译通过报告

**验收标准**:
1. `mvn clean install -DskipTests` 全模块通过
2. `npm install` 在两个前端项目都通过
3. 不存在循环依赖
4. 所有模块可独立 mvn compile

**Codex Prompt 要点**:
- 修复编译错误
- 检查模块间依赖关系
- 确保 ruoyi-common 被所有模块正确引用
