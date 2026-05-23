CREATE TABLE processed_domain_events (
    event_id UUID PRIMARY KEY,
    consumer_name VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    processed_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_processed_domain_events_consumer_processed_at
    ON processed_domain_events(consumer_name, processed_at DESC);
