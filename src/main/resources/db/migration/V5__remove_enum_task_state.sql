CREATE TABLE tmp_task_state(
	task_id TEXT,
	task_state TEXT
);

INSERT INTO tmp_task_state
SELECT task_id, task_state::TEXT FROM task;

ALTER TABLE task DROP COLUMN task_state;
ALTER TABLE task ADD COLUMN task_state TEXT;

UPDATE task SET task_state=tmp_task_state.task_state
FROM tmp_task_state WHERE tmp_task_state.task_id = task.task_id;

DROP TABLE tmp_task_state;

DROP TYPE task_state;