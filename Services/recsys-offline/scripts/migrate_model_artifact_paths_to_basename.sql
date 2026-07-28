-- One-time: convert host-absolute model_artifacts.artifact_path to portable basename.
-- Safe to re-run: rows that are already basenames are unchanged.
--
-- Example:
--   docker exec -i postgres-social psql -U postgres -d social_db < migrate_model_artifact_paths_to_basename.sql

UPDATE model_artifacts
SET artifact_path = regexp_replace(artifact_path, '^.*[\\/]', '')
WHERE artifact_path ~ '[\\/]'
   OR artifact_path ~ '^[A-Za-z]:';

-- Optional check:
-- SELECT model_name, version, artifact_path, is_active FROM model_artifacts ORDER BY model_name, version;
