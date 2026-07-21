-- =====================================================================
-- Recsys offline training / serving support tables
-- model_artifacts, post_impression_log, user_seen_posts
-- =====================================================================

CREATE TABLE model_artifacts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    model_name VARCHAR(64) NOT NULL,
    version INT NOT NULL,
    format VARCHAR(32) NOT NULL,
    artifact_path TEXT NOT NULL,
    metrics JSONB,
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    trained_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_model_artifacts_model_version UNIQUE (model_name, version)
);

CREATE UNIQUE INDEX uk_model_artifacts_one_active
    ON model_artifacts (model_name)
    WHERE is_active = TRUE;

CREATE INDEX idx_model_artifacts_active
    ON model_artifacts (model_name, is_active);


CREATE TABLE post_impression_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    post_id VARCHAR(64) NOT NULL,
    shown_at TIMESTAMP NOT NULL DEFAULT NOW(),
    rank_position INT,
    model_version INT,
    model_name VARCHAR(64),
    request_id VARCHAR(64)
);

CREATE INDEX idx_post_impression_user_shown
    ON post_impression_log (user_id, shown_at DESC);

CREATE INDEX idx_post_impression_post_shown
    ON post_impression_log (post_id, shown_at DESC);

CREATE INDEX idx_post_impression_user_post_shown
    ON post_impression_log (user_id, post_id, shown_at);


CREATE TABLE user_seen_posts (
    user_id UUID NOT NULL,
    post_id VARCHAR(64) NOT NULL,
    seen_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_user_seen_posts PRIMARY KEY (user_id, post_id)
);

CREATE INDEX idx_user_seen_posts_user_seen_at
    ON user_seen_posts (user_id, seen_at DESC);
