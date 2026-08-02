-- =====================================================================
-- Commerce Home hybrid LTR: CF / AR / social export / impressions / model
-- Required columns + indexes per openspec commerce-home-hybrid-ltr (D5/D12/D13/D14/D20)
-- =====================================================================

CREATE TABLE entity_cooccur (
    entity_type   VARCHAR(32) NOT NULL,
    entity_id     UUID NOT NULL,
    neighbor_type VARCHAR(32) NOT NULL,
    neighbor_id   UUID NOT NULL,
    score         DOUBLE PRECISION NOT NULL,
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (entity_type, entity_id, neighbor_type, neighbor_id)
);

CREATE INDEX idx_entity_cooccur_entity_score
    ON entity_cooccur (entity_type, entity_id, score DESC);


CREATE TABLE social_tag_category_ar (
    tag_type    VARCHAR(16) NOT NULL,
    tag         VARCHAR(128) NOT NULL,
    category_id UUID NOT NULL,
    support     DOUBLE PRECISION NOT NULL,
    confidence  DOUBLE PRECISION NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (tag_type, tag, category_id)
);

CREATE INDEX idx_social_tag_category_ar_tag
    ON social_tag_category_ar (tag_type, tag);


CREATE TABLE user_social_interest_export (
    user_id     UUID NOT NULL,
    tag_type    VARCHAR(16) NOT NULL,
    tag         VARCHAR(128) NOT NULL,
    score       DOUBLE PRECISION NOT NULL,
    window_days INT NOT NULL DEFAULT 90,
    computed_at TIMESTAMPTZ NOT NULL,
    as_of       TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (user_id, tag_type, tag)
);

CREATE INDEX idx_user_social_interest_export_user
    ON user_social_interest_export (user_id);


CREATE TABLE home_impression_log (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id        UUID NOT NULL,
    product_id     UUID NOT NULL,
    shown_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    rank_position  INT,
    model_name     VARCHAR(64),
    model_version  INT,
    request_id     VARCHAR(64) NOT NULL,
    ranking_mode   VARCHAR(16) NOT NULL,
    sources        JSONB NOT NULL DEFAULT '[]'::jsonb,
    personal_score DOUBLE PRECISION,
    cf_score       DOUBLE PRECISION,
    ar_score       DOUBLE PRECISION
);

CREATE INDEX idx_home_impression_user_shown
    ON home_impression_log (user_id, shown_at DESC);

CREATE INDEX idx_home_impression_user_product_shown
    ON home_impression_log (user_id, product_id, shown_at);

CREATE INDEX idx_home_impression_request
    ON home_impression_log (request_id);


CREATE TABLE home_engage_event (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL,
    product_id  UUID NOT NULL,
    event_type  VARCHAR(16) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    request_id  VARCHAR(64)
);

CREATE INDEX idx_home_engage_user_product_occurred
    ON home_engage_event (user_id, product_id, occurred_at);


CREATE TABLE model_artifacts (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    model_name    VARCHAR(64) NOT NULL,
    version       INT NOT NULL,
    format        VARCHAR(32) NOT NULL,
    artifact_path TEXT NOT NULL,
    metrics       JSONB,
    is_active     BOOLEAN NOT NULL DEFAULT FALSE,
    trained_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_commerce_model_artifacts_model_version UNIQUE (model_name, version)
);

CREATE UNIQUE INDEX uk_commerce_model_artifacts_one_active
    ON model_artifacts (model_name)
    WHERE is_active = TRUE;

CREATE INDEX idx_commerce_model_artifacts_active
    ON model_artifacts (model_name, is_active);
