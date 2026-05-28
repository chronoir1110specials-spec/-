# 阶段4：RAG 知识库

**目标**: 实现文档上传、文本切片、向量化、语义检索、知识库问答。

**依赖**: 阶段2 全部完成
**设计文档参考**: 第9章（RAG 知识库设计）

---

## 任务 4.1: pgvector 扩展安装 + 环境配置

**目标**: 在 PostgreSQL 中启用 pgvector 扩展，创建向量表。

**输入**: 设计文档 §9.6 向量数据库选择
**输出**:
- pgvector 扩展已启用
- 向量表 SQL
- EmbeddingConfig 配置类

**验收标准**:
1. PostgreSQL 安装 pgvector 扩展
2. 创建 kb_chunk_vector 表: id, chunk_id, embedding vector(N)
3. N 取 Embedding 模型返回的实际维度
4. 向量索引: IVFFlat 或 HNSW（数据量少时用余弦距离即可）

**Codex Prompt 要点**:
- 需要先安装 PostgreSQL（我们 VPS 已有 MySQL，pgvector 可能需要额外安装）
- 如果安装过重，先用 Chroma 的 HTTP API 方案
- Maven 不需要额外依赖，HTTP 调用 Chroma REST API

---

## 任务 4.2: 知识库文档实体 + 上传

**目标**: 实现知识库文档的上传和管理。

**输入**: 设计文档 §10.7 + §10.8
**输出**:
- ruoyi-knowledge/src/main/java/.../entity/KbDocument.java
- ruoyi-knowledge/src/main/java/.../entity/KbChunk.java
- ruoyi-knowledge/src/main/java/.../mapper/ 对应 Mapper
- ruoyi-knowledge/src/main/java/.../controller/KbDocumentController.java

**验收标准**:
1. POST /kb/document/upload 上传文件（PDF/DOCX/TXT/MD）
2. 文件保存到受控目录
3. 解析文本内容存入 kb_document
4. parse_status 记录解析状态
5. 管理员权限校验

---

## 任务 4.3: 文本切片服务

**目标**: 实现文档文本切片，按 chunk_size 分段。

**输入**: 设计文档 §9.5 文本切片策略
**输出**: ruoyi-knowledge/src/main/java/.../service/TextChunkService.java

**验收标准**:
1. chunk_size: 500-800 字符
2. chunk_overlap: 80-150 字符
3. 按段落边界优先切割（不切断句子）
4. 每个 chunk 计算 content_hash
5. 保存到 kb_chunk 表
6. 元数据: 文档标题、页码、段落序号

**Codex Prompt 要点**:
- 先用简单规则（按段落 + 长度），不引入复杂的语义分割
- LangChain 不必要，纯 Java 实现

---

## 任务 4.4: Embedding 服务封装

**目标**: 实现文本向量化调用。

**输入**: 设计文档 §9.7 Embedding 向量化方案
**输出**:
- ruoyi-knowledge/src/main/java/.../service/EmbeddingService.java
- ruoyi-knowledge/src/main/java/.../dto/EmbeddingResult.java

**验收标准**:
1. embed(String text) 返回向量
2. embedBatch(List<String>) 批量向量化
3. 调用 External Embedding API（DigitalOcean 或其他）
4. 记录调用耗时和 Token
5. API 调用失败时有重试和错误日志

**Codex Prompt 要点**:
- 优先用 DigitalOcean Embedding API（设计文档 §9.7.1）
- 先验证 DO 是否真的提供了 embedding 端点
- 如果没有，备选：硅基流动或阿里云的 embedding API

---

## 任务 4.5: 文档向量化入库流程

**目标**: 实现文档解析→切片→向量化→入库完整流程。

**输入**: 任务 4.3 + 4.4 完成
**输出**: ruoyi-knowledge/src/main/java/.../service/DocumentVectorizeService.java

**验收标准**:
1. 文档上传后异步触发向量化
2. 内容哈希跳过已存在的 chunk
3. 批量调用 Embedding API
4. 向量存入 Chroma（HTTP API）或 pgvector
5. chunk.vector_status 更新为 success/failed
6. 向量化失败可重试

---

## 任务 4.6: Chroma HTTP 客户端（备选方案）

**目标**: 如果不用 pgvector，实现 Chroma 的 Java HTTP 客户端。

**输入**: Chroma REST API 文档
**输出**: ruoyi-knowledge/src/main/java/.../vectorstore/ChromaClient.java

**验收标准**:
1. createCollection(name, dimension)
2. addVectors(collectionId, vectors[], metadatas[])
3. query(collectionId, queryVector, topK)
4. 通过 HTTP + RestTemplate 实现，不依赖第三方 SDK
5. 错误处理完整

**注意**: 此任务仅在 pgvector 方案不可行时执行

---

## 任务 4.7: 知识库检索服务

**目标**: 实现语义检索，将用户问题向量化后检索引文片段。

**输入**: 任务 4.5 完成
**输出**: ruoyi-knowledge/src/main/java/.../service/KbSearchService.java

**验收标准**:
1. search(query, topK) 返回 TopK 相似片段
2. similarity_threshold 过滤低质量结果
3. 返回结果包含: 片段内容、文档标题、引用来源
4. 检索日志记录

---

## 任务 4.8: 知识库问答 Agent

**目标**: 实现 RAG 问答，检索→拼 Prompt→调用模型→返回答案+引用。

**输入**: 任务 4.7 完成
**输出**:
- ruoyi-agent/src/main/java/.../agent/KbQaAgent.java
- ruoyi-agent/src/main/java/.../prompt/KbQaPrompts.java

**验收标准**:
1. 问题向量化 → 检索 TopK 片段 → 拼入 Prompt → 调用模型
2. System Prompt 要求基于检索片段回答
3. 返回答案 + 引用来源（文档标题、片段序号）
4. 检索结果为空时明确提示"知识库未检索到相关资料"
5. Agent 任务日志和步骤日志记录

---

## 任务 4.9: 知识库问答接口

**目标**: 暴露知识库问答 API。

**输入**: 任务 4.8 完成
**输出**: ruoyi-knowledge/src/main/java/.../controller/KbQaController.java

**验收标准**:
1. POST /kb/ask 接受 question 参数
2. 返回答案 + citations[]
3. 支持流式 SSE
4. curl 验证

---

## 任务 4.10: 知识库管理接口整合

**目标**: 完善知识库后台管理接口。

**输入**: 已有接口
**输出**: 补充缺失的接口

**验收标准**:
1. GET /kb/document/list 分页查询
2. DELETE /kb/document/{id} 删除文档及所有 chunk
3. POST /kb/document/{id}/reindex 重新向量化
4. GET /kb/stats 知识库统计
