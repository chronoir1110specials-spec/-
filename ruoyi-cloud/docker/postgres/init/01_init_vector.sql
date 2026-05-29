CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE kb_chunk_vector (
  id                   BIGSERIAL PRIMARY KEY,
  chunk_id             BIGINT NOT NULL UNIQUE,
  embedding_vector     vector(768),
  embedding_model      VARCHAR(100),
  embedding_dimension  INT,
  vector_status        VARCHAR(30),
  create_time          TIMESTAMP,
  update_time          TIMESTAMP,
  deleted              SMALLINT DEFAULT 0
);

CREATE INDEX idx_kb_chunk_vector_embedding ON kb_chunk_vector
  USING ivfflat (embedding_vector vector_cosine_ops)
  WITH (lists = 100);

CREATE INDEX idx_kb_chunk_vector_chunk_id ON kb_chunk_vector(chunk_id);
CREATE INDEX idx_kb_chunk_vector_deleted ON kb_chunk_vector(deleted);
