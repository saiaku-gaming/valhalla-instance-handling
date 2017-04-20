CREATE TABLE tmp_framework (
	id TEXT,
	ts TIMESTAMP
);

INSERT INTO tmp_framework SELECT * FROM mesos_framework;

DELETE FROM mesos_framework;

ALTER TABLE mesos_framework ADD PRIMARY KEY(id);

INSERT INTO mesos_framework
	SELECT DISTINCT ON (tf.id) tf.id, tf.ts 
		FROM tmp_framework tf
	ORDER BY tf.id DESC, tf.ts DESC;

DROP TABLE tmp_framework;