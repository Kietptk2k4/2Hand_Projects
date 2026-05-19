-- =====================================================================
-- SOCIAL SERVICE DATABASE SCHEMA (RELATIONAL PART)
-- PostgreSQL
-- =====================================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =====================================================================
-- ENUMS
-- =====================================================================

CREATE TYPE follow_status AS ENUM (
    'PENDING',
    'ACCEPTED'
);

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
    post_id VARCHAR(64) NOT NULL,
    user_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (post_id, user_id)
);

-- =====================================================================
-- POST SAVES
-- =====================================================================

CREATE TABLE post_saves (
    post_id VARCHAR(64) NOT NULL,
    user_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (post_id, user_id)
);

-- =====================================================================
-- COMMENT REACTION
-- =====================================================================

CREATE TABLE comment_reaction (
    comment_id VARCHAR(64) NOT NULL,
    user_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (comment_id, user_id)
);

-- =====================================================================
-- FOLLOWS
-- =====================================================================

CREATE TABLE follows (
    follower_id UUID NOT NULL,
    followee_id UUID NOT NULL,
    status follow_status NOT NULL DEFAULT 'ACCEPTED',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (follower_id, followee_id),
    CONSTRAINT ck_follows_not_self CHECK (follower_id <> followee_id)
);

-- =====================================================================
-- SEARCH HISTORY
-- =====================================================================

CREATE TABLE search_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    keyword VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =====================================================================
-- OUTBOX EVENTS
-- =====================================================================

CREATE TABLE outbox_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type VARCHAR(255) NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    payload JSONB NOT NULL,
    status outbox_status NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_error TEXT,
    published_at TIMESTAMP
);

-- =====================================================================
-- INDEXES
-- =====================================================================

CREATE INDEX idx_follows_follower_created
ON follows(follower_id, created_at DESC);

CREATE INDEX idx_follows_followee
ON follows(followee_id);

CREATE INDEX idx_search_history_user_created
ON search_history(user_id, created_at DESC);

CREATE INDEX idx_outbox_status
ON outbox_events(status);
