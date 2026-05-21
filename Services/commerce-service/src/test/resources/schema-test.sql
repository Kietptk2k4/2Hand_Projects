CREATE TABLE IF NOT EXISTS outbox_events (
    id UUID PRIMARY KEY,
    event_type VARCHAR(255) NOT NULL,
    event_key VARCHAR(255) NOT NULL UNIQUE,
    aggregate_id UUID NOT NULL,
    source VARCHAR(100) NOT NULL,
    payload VARCHAR(4000) NOT NULL,
    status VARCHAR(50) NOT NULL,
    retry_count INTEGER NOT NULL DEFAULT 0,
    last_error VARCHAR(2000),
    created_at TIMESTAMP NOT NULL,
    published_at TIMESTAMP
);
