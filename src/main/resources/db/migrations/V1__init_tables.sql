BEGIN;

CREATE TABLE instance (
  id SERIAL PRIMARY KEY,
  ts TIMESTAMP DEFAULT NOW() NOT NULL,
  level TEXT NOT NULL,
  version TEXT NOT NULL,
  state TEXT NOT NULL,
  address TEXT,
  port INTEGER,
  task_id TEXT
);


COMMIT;