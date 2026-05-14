-- =====================================================================
-- SOCIAL SERVICE (POSTGRESQL)
-- =====================================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =====================================================================
-- ENUMS
-- =====================================================================

CREATE TYPE outbox_status AS ENUM (
    'PENDING',
    'PROCESSING',
    'PUBLISHED',
    'FAILED'
);

-- =====================================================================
-- POST LIKES
-- =====================================================================

CREATE TABLE post_likes (
    post_id        VARCHAR(64) NOT NULL,
    user_id        UUID NOT NULL,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_post_likes UNIQUE(post_id, user_id)
);

CREATE INDEX idx_post_likes_post
ON post_likes(post_id);

CREATE INDEX idx_post_likes_user
ON post_likes(user_id);

-- =====================================================================
-- POST SAVES
-- =====================================================================

CREATE TABLE post_saves (
    post_id        VARCHAR(64) NOT NULL,
    user_id        UUID NOT NULL,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_post_saves UNIQUE(post_id, user_id)
);

CREATE INDEX idx_post_saves_post
ON post_saves(post_id);

CREATE INDEX idx_post_saves_user
ON post_saves(user_id);

-- =====================================================================
-- SEARCH HISTORY
-- =====================================================================

CREATE TABLE search_history (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id        UUID NOT NULL,
    keyword        VARCHAR(255) NOT NULL,

    created_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_search_history_user_created
ON search_history(user_id, created_at DESC);

-- =====================================================================
-- COMMENT REACTIONS
-- =====================================================================

CREATE TABLE comment_reactions (
    comment_id     VARCHAR(64) NOT NULL,
    user_id        UUID NOT NULL,

    created_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_comment_reactions UNIQUE(comment_id, user_id)
);

CREATE INDEX idx_comment_reactions_comment
ON comment_reactions(comment_id);

CREATE INDEX idx_comment_reactions_user
ON comment_reactions(user_id);

-- =====================================================================
-- FOLLOWS
-- =====================================================================

CREATE TABLE follows (
    follower_id    UUID NOT NULL,
    followee_id    UUID NOT NULL,

    created_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_follows UNIQUE(follower_id, followee_id),

    CONSTRAINT chk_no_self_follow
    CHECK (follower_id <> followee_id)
);

CREATE INDEX idx_follows_follower_created
ON follows(follower_id, created_at DESC);

CREATE INDEX idx_follows_followee
ON follows(followee_id);

-- =====================================================================
-- OUTBOX EVENTS
-- =====================================================================

CREATE TABLE outbox_events (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    event_type     VARCHAR(100) NOT NULL,
    aggregate_id   VARCHAR(64) NOT NULL,

    payload        JSONB NOT NULL,

    status         outbox_status NOT NULL DEFAULT 'PENDING',

    retry_count    INTEGER NOT NULL DEFAULT 0,

    created_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    published_at   TIMESTAMP WITH TIME ZONE,

    last_error     TEXT
);

CREATE INDEX idx_outbox_status_created
ON outbox_events(status, created_at);

CREATE INDEX idx_outbox_aggregate
ON outbox_events(aggregate_id);

-- =====================================================================
-- UPDATED_AT AUTO UPDATE TRIGGER
-- =====================================================================

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- search_history
CREATE TRIGGER trg_search_history_updated_at
BEFORE UPDATE ON search_history
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- comment_reactions
CREATE TRIGGER trg_comment_reactions_updated_at
BEFORE UPDATE ON comment_reactions
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- =====================================================================
-- OPTIONAL CLEANUP INDEXES
-- =====================================================================

CREATE INDEX idx_outbox_pending_retry
ON outbox_events(status, retry_count)
WHERE status IN ('PENDING', 'FAILED');

CREATE INDEX idx_search_history_keyword
ON search_history(keyword);
