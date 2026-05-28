SET NAMES utf8mb4;

-- ----------------------------
-- 先导入若依基础表
-- ----------------------------
SOURCE ry_20260417.sql;
SOURCE ry_config_20260311.sql;
SOURCE quartz.sql;

-- ----------------------------
-- 1、用户画像表
-- ----------------------------
drop table if exists user_profile;
create table user_profile (
  id                 bigint          not null auto_increment    comment '主键',
  user_id            bigint          not null                   comment '用户 ID',
  school             varchar(100)    default null               comment '学校',
  major              varchar(100)    default null               comment '专业',
  grade              varchar(50)     default null               comment '年级',
  target_position    varchar(100)    default null               comment '目标岗位',
  target_city        varchar(100)    default null               comment '目标城市',
  skill_tags         varchar(500)    default null               comment '技能标签',
  project_tags       varchar(500)    default null               comment '项目标签',
  job_stage          varchar(50)     default null               comment '求职阶段',
  create_time        datetime                                   comment '创建时间',
  update_time        datetime                                   comment '更新时间',
  deleted            tinyint(1)      default 0                  comment '是否删除',
  primary key (id),
  key idx_user_profile_user_id (user_id),
  key idx_user_profile_job_stage (job_stage),
  key idx_user_profile_target_position (target_position)
) engine=InnoDB default charset=utf8mb4 comment='用户画像表';

-- ----------------------------
-- 2、会话表
-- ----------------------------
drop table if exists chat_session;
create table chat_session (
  id                       bigint          not null auto_increment    comment '主键',
  user_id                  bigint          not null                   comment '用户 ID',
  title                    varchar(200)    default null               comment '会话标题',
  agent_type               varchar(50)     default null               comment 'Agent 类型',
  context_summary          text                                       comment '会话历史摘要，用于上下文压缩',
  last_summary_message_id  bigint          default null               comment '上次摘要覆盖到的消息 ID',
  summary_version          int             default 0                  comment '摘要版本',
  recent_message_limit     int             default null               comment '最近消息保留数量',
  max_context_tokens       int             default null               comment '会话上下文最大 Token 预算',
  interview_status         varchar(50)     default null               comment '模拟面试状态',
  current_question_index   int             default null               comment '当前面试题序号',
  total_questions          int             default null               comment '面试总题数',
  interview_position       varchar(100)    default null               comment '面试岗位方向',
  interview_difficulty     varchar(50)     default null               comment '面试难度',
  interview_score          int             default null               comment '面试综合评分',
  interview_summary        text                                       comment '面试总结',
  create_time              datetime                                   comment '创建时间',
  update_time              datetime                                   comment '更新时间',
  deleted                  tinyint(1)      default 0                  comment '是否删除',
  primary key (id),
  key idx_chat_session_user_id (user_id),
  key idx_chat_session_agent_type (agent_type),
  key idx_chat_session_interview_status (interview_status),
  key idx_chat_session_create_time (create_time)
) engine=InnoDB default charset=utf8mb4 comment='会话表';

-- ----------------------------
-- 3、消息表
-- ----------------------------
drop table if exists chat_message;
create table chat_message (
  id            bigint          not null auto_increment    comment '主键',
  session_id    bigint          not null                   comment '会话 ID',
  user_id       bigint          not null                   comment '用户 ID',
  role          varchar(20)     not null                   comment 'user / assistant / system',
  content       text                                       comment '消息内容',
  model_name    varchar(100)    default null               comment '使用模型',
  create_time   datetime                                   comment '创建时间',
  deleted       tinyint(1)      default 0                  comment '是否删除',
  primary key (id),
  key idx_chat_message_session_id (session_id),
  key idx_chat_message_user_id (user_id),
  key idx_chat_message_create_time (create_time),
  key idx_chat_message_session_time (session_id, create_time)
) engine=InnoDB default charset=utf8mb4 comment='消息表';

-- ----------------------------
-- 4、简历表
-- ----------------------------
drop table if exists resume_info;
create table resume_info (
  id                  bigint          not null auto_increment    comment '主键',
  user_id             bigint          not null                   comment '用户 ID',
  resume_name         varchar(200)    default null               comment '简历名称',
  original_file_name  varchar(255)    default null               comment '原始文件名',
  file_type           varchar(20)     default null               comment '文件类型：pdf / docx / doc / text',
  file_url            varchar(500)    default null               comment '原始文件保存路径',
  content_hash        varchar(128)    default null               comment '简历文本 hash',
  content             longtext                                   comment '简历文本内容',
  parse_status        varchar(30)     default null               comment '解析状态：pending / success / failed',
  parse_error         text                                       comment '解析失败原因',
  target_position     varchar(100)    default null               comment '目标岗位',
  analysis_result     longtext                                   comment '分析结果',
  score               int             default null               comment '简历评分',
  create_time         datetime                                   comment '创建时间',
  update_time         datetime                                   comment '更新时间',
  deleted             tinyint(1)      default 0                  comment '是否删除',
  primary key (id),
  key idx_resume_info_user_id (user_id),
  key idx_resume_info_content_hash (content_hash),
  key idx_resume_info_parse_status (parse_status),
  key idx_resume_info_target_position (target_position)
) engine=InnoDB default charset=utf8mb4 comment='简历表';

-- ----------------------------
-- 5、岗位信息表
-- ----------------------------
drop table if exists job_info;
create table job_info (
  id               bigint          not null auto_increment    comment '主键',
  user_id          bigint          not null                   comment '用户 ID',
  job_name         varchar(200)    default null               comment '岗位名称',
  company_name     varchar(200)    default null               comment '公司名称',
  job_description  longtext                                   comment '岗位 JD',
  analysis_result  longtext                                   comment '分析结果',
  match_score      int             default null               comment '匹配评分',
  create_time      datetime                                   comment '创建时间',
  deleted          tinyint(1)      default 0                  comment '是否删除',
  primary key (id),
  key idx_job_info_user_id (user_id),
  key idx_job_info_job_name (job_name),
  key idx_job_info_company_name (company_name),
  key idx_job_info_create_time (create_time)
) engine=InnoDB default charset=utf8mb4 comment='岗位信息表';

-- ----------------------------
-- 6、知识库文档表
-- ----------------------------
drop table if exists kb_document;
create table kb_document (
  id                bigint          not null auto_increment    comment '主键',
  title             varchar(255)    default null               comment '文档标题',
  file_name         varchar(255)    default null               comment '文件名',
  file_type         varchar(20)     default null               comment '文件类型',
  file_url          varchar(500)    default null               comment '文件地址',
  content_hash      varchar(128)    default null               comment '文档内容 hash，用于判断是否重复上传',
  parse_status      varchar(30)     default null               comment '解析状态：pending / success / failed',
  embedding_status  varchar(30)     default null               comment '向量化状态：pending / processing / success / failed',
  chunk_count       int             default 0                  comment '文档切片数量',
  create_user       bigint          default null               comment '上传用户',
  create_time       datetime                                   comment '创建时间',
  update_time       datetime                                   comment '更新时间',
  deleted           tinyint(1)      default 0                  comment '是否删除',
  primary key (id),
  key idx_kb_document_content_hash (content_hash),
  key idx_kb_document_parse_status (parse_status),
  key idx_kb_document_embedding_status (embedding_status),
  key idx_kb_document_create_user (create_user)
) engine=InnoDB default charset=utf8mb4 comment='知识库文档表';

-- ----------------------------
-- 7、知识片段表
-- ----------------------------
drop table if exists kb_chunk;
create table kb_chunk (
  id                   bigint          not null auto_increment    comment '主键',
  document_id          bigint          not null                   comment '文档 ID',
  chunk_index          int             not null                   comment '片段序号',
  content              text                                       comment '文本片段',
  content_hash         varchar(128)    default null               comment '片段内容 hash',
  vector_id            varchar(128)    default null               comment '向量 ID',
  embedding_model      varchar(100)    default null               comment '使用的 Embedding 模型',
  embedding_dimension  int             default null               comment '向量维度',
  chunk_version        int             default 1                  comment '切片版本',
  token_count          int             default null               comment '片段 Token 数',
  vector_status        varchar(30)     default null               comment '向量化状态',
  metadata             json                                       comment '元数据',
  create_time          datetime                                   comment '创建时间',
  update_time          datetime                                   comment '更新时间',
  deleted              tinyint(1)      default 0                  comment '是否删除',
  primary key (id),
  key idx_kb_chunk_document_id (document_id),
  key idx_kb_chunk_content_hash (content_hash),
  key idx_kb_chunk_vector_id (vector_id),
  key idx_kb_chunk_vector_status (vector_status),
  key idx_kb_chunk_embedding (embedding_model, embedding_dimension, chunk_version),
  unique key uk_kb_chunk_document_index (document_id, chunk_index)
) engine=InnoDB default charset=utf8mb4 comment='知识片段表';

-- ----------------------------
-- 8、模型配置表
-- ----------------------------
drop table if exists model_config;
create table model_config (
  id                   bigint          not null auto_increment    comment '主键',
  model_role           varchar(30)     not null                   comment 'primary / fallback / embedding',
  provider             varchar(50)     not null                   comment 'digitalocean / glm',
  model_name           varchar(100)    not null                   comment '模型名称',
  base_url             varchar(500)    not null                   comment 'API 地址',
  api_key              varchar(500)    not null                   comment 'API Key，加密存储',
  enabled              tinyint(1)      default 1                  comment '是否启用',
  max_tokens           int             default null               comment '最大输出长度',
  embedding_dimension  int             default null               comment 'Embedding 模型向量维度，仅向量模型使用',
  timeout              int             default null               comment '超时时间',
  create_time          datetime                                   comment '创建时间',
  deleted              tinyint(1)      default 0                  comment '是否删除',
  primary key (id),
  key idx_model_config_role_enabled (model_role, enabled),
  key idx_model_config_provider (provider),
  key idx_model_config_model_name (model_name)
) engine=InnoDB default charset=utf8mb4 comment='模型配置表';

-- ----------------------------
-- 9、模型调用日志表
-- ----------------------------
drop table if exists model_call_log;
create table model_call_log (
  id                 bigint          not null auto_increment    comment '主键',
  user_id            bigint          default null               comment '用户 ID',
  session_id         bigint          default null               comment '会话 ID',
  provider           varchar(50)     default null               comment '模型提供商',
  model_name         varchar(100)    default null               comment '模型名称',
  is_fallback        tinyint(1)      default 0                  comment '是否兜底调用',
  prompt_tokens      int             default null               comment '输入 Token',
  completion_tokens  int             default null               comment '输出 Token',
  total_tokens       int             default null               comment '总 Token',
  cost_time          int             default null               comment '耗时，毫秒',
  status             varchar(30)     default null               comment '调用状态',
  error_message      text                                       comment '错误信息',
  create_time        datetime                                   comment '创建时间',
  deleted            tinyint(1)      default 0                  comment '是否删除',
  primary key (id),
  key idx_model_call_log_user_id (user_id),
  key idx_model_call_log_session_id (session_id),
  key idx_model_call_log_provider (provider),
  key idx_model_call_log_status (status),
  key idx_model_call_log_create_time (create_time)
) engine=InnoDB default charset=utf8mb4 comment='模型调用日志表';

-- ----------------------------
-- 10、Agent 任务表
-- ----------------------------
drop table if exists agent_task;
create table agent_task (
  id                 bigint          not null auto_increment    comment '主键',
  user_id            bigint          not null                   comment '用户 ID',
  session_id         bigint          default null               comment '会话 ID',
  agent_type         varchar(50)     not null                   comment 'Agent 类型',
  task_type          varchar(50)     not null                   comment '任务类型',
  execution_mode     varchar(50)     default null               comment 'single_call / workflow / agentic_loop',
  input_summary      varchar(1000)   default null               comment '输入摘要，避免保存敏感全文',
  output_summary     varchar(1000)   default null               comment '输出摘要',
  status             varchar(30)     default null               comment 'pending / running / succeeded / failed / cancelled / timeout',
  current_step       varchar(100)    default null               comment '当前步骤',
  total_tool_calls   int             default 0                  comment '工具调用总次数',
  total_tokens       int             default 0                  comment '总 Token 消耗',
  total_cost_time    int             default 0                  comment '总耗时，毫秒',
  error_code         varchar(100)    default null               comment '错误码',
  error_message      text                                       comment '脱敏后的错误信息',
  create_time        datetime                                   comment '创建时间',
  update_time        datetime                                   comment '更新时间',
  finish_time        datetime                                   comment '完成时间',
  deleted            tinyint(1)      default 0                  comment '是否删除',
  primary key (id),
  key idx_agent_task_user_id (user_id),
  key idx_agent_task_session_id (session_id),
  key idx_agent_task_agent_type (agent_type),
  key idx_agent_task_task_type (task_type),
  key idx_agent_task_status (status),
  key idx_agent_task_create_time (create_time)
) engine=InnoDB default charset=utf8mb4 comment='Agent 任务表';

-- ----------------------------
-- 11、Agent 步骤日志表
-- ----------------------------
drop table if exists agent_step_log;
create table agent_step_log (
  id                 bigint          not null auto_increment    comment '主键',
  task_id            bigint          not null                   comment 'Agent 任务 ID',
  user_id            bigint          not null                   comment '用户 ID',
  session_id         bigint          default null               comment '会话 ID',
  agent_type         varchar(50)     not null                   comment 'Agent 类型',
  step_index         int             not null                   comment '步骤序号',
  step_type          varchar(50)     default null               comment 'model_call / tool_call / rag_search / output_parse',
  step_name          varchar(100)    default null               comment '模型名、工具名或解析器名称',
  input_summary      varchar(1000)   default null               comment '输入摘要',
  output_summary     varchar(1000)   default null               comment '输出摘要',
  status             varchar(30)     default null               comment 'success / failed / skipped',
  prompt_tokens      int             default null               comment '输入 Token',
  completion_tokens  int             default null               comment '输出 Token',
  total_tokens       int             default null               comment '总 Token',
  cost_time          int             default null               comment '耗时，毫秒',
  error_code         varchar(100)    default null               comment '错误码',
  error_message      text                                       comment '脱敏后的错误信息',
  create_time        datetime                                   comment '创建时间',
  deleted            tinyint(1)      default 0                  comment '是否删除',
  primary key (id),
  key idx_agent_step_log_task_id (task_id),
  key idx_agent_step_log_user_id (user_id),
  key idx_agent_step_log_session_id (session_id),
  key idx_agent_step_log_agent_type (agent_type),
  key idx_agent_step_log_step_type (step_type),
  key idx_agent_step_log_status (status),
  key idx_agent_step_log_create_time (create_time)
) engine=InnoDB default charset=utf8mb4 comment='Agent 步骤日志表';

-- ----------------------------
-- 预置-模型配置表数据
-- ----------------------------
insert into model_config (model_role, provider, model_name, base_url, api_key, enabled, max_tokens, embedding_dimension, timeout, create_time, deleted)
values ('primary', 'digitalocean', 'deepseek-v4-pro', 'https://inference.do-ai.run/v1', 'PLACEHOLDER', 1, 4096, null, 60000, sysdate(), 0);

insert into model_config (model_role, provider, model_name, base_url, api_key, enabled, max_tokens, embedding_dimension, timeout, create_time, deleted)
values ('fallback', 'glm', 'glm-5.1', 'https://open.bigmodel.cn/api/paas/v4', 'PLACEHOLDER', 1, 4096, null, 60000, sysdate(), 0);
