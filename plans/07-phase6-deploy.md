# 阶段6：Docker Compose 部署

**目标**: 将系统容器化并部署到 VPS。

**依赖**: 阶段3 核心功能完成（阶段4、5可并行）
**设计文档参考**: 第14章（部署设计）

---

## 任务 6.1: Docker 环境安装

**目标**: 在 VPS 上安装 Docker + Docker Compose。

**输入**: 无
**输出**: Docker 环境就绪

**验收标准**:
1. `docker --version` 正常
2. `docker compose version` 正常
3. Docker daemon 运行中

---

## 任务 6.2: 后端 Dockerfile 编写

**目标**: 为每个后端模块编写 Dockerfile。

**输入**: Maven 构建产物
**输出**:
- ruoyi-gateway/Dockerfile
- ruoyi-auth/Dockerfile
- ruoyi-system/Dockerfile
- ruoyi-agent/Dockerfile
- ruoyi-knowledge/Dockerfile

**验收标准**:
1. 基于 openjdk:17-slim
2. 复制 JAR 到容器
3. 使用非 root 用户运行
4. 暴露正确的端口
5. 健康检查配置

---

## 任务 6.3: 前端 Dockerfile 编写

**目标**: 为前端项目编写 Dockerfile 或直接用 Nginx 托管。

**输入**: npm build 产物
**输出**: ai-web/Dockerfile

**验收标准**:
1. 基于 nginx:alpine
2. 复制 dist 到 /usr/share/nginx/html
3. 配置 SPA 路由（try_files）
4. 配置反向代理 /api/ → Gateway

---

## 任务 6.4: docker-compose.yml 编写

**目标**: 编写完整的 Docker Compose 编排文件。

**输入**: 设计文档 §14.5
**输出**: docker/docker-compose.yml

**验收标准**:
1. 包含所有服务容器
2. Nginx 只暴露 80/443
3. 其他服务只在内部网络通信
4. 环境变量从 .env 读取
5. 依赖关系正确（depends_on）
6. 数据卷持久化（MySQL、Redis、文件上传）

**Codex Prompt 要点**:
- 严格遵循设计文档 §14.5 的示例
- 生产环境不做端口映射（除 Nginx）
- MySQL、Redis、Nacos 使用固定版本镜像

---

## 任务 6.5: Nginx 配置编写

**目标**: 编写 Nginx 反向代理配置。

**输入**: 设计文档 §15.3
**输出**: docker/nginx.conf

**验收标准**:
1. HTTP → HTTPS 重定向
2. /api/ 反向代理到 Gateway:8080
3. 静态资源由 Nginx 直出
4. 安全头配置
5. 上传大小限制 10MB
6. WebSocket 支持（SSE 长连接）

---

## 任务 6.6: .env.example + 部署文档

**目标**: 编写环境变量模板和部署说明。

**输入**: 所有需要配置的环境变量
**输出**:
- docker/.env.example
- docs/deploy-guide.md

**验收标准**:
1. .env.example 包含所有必需变量（不填真实值）
2. 部署文档包含: 环境要求、部署步骤、验证方法
3. 部署文档包含安全提醒（改密码、关端口、配 HTTPS）
