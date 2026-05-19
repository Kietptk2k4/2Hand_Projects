ALTER TABLE outbox_events
    ADD COLUMN IF NOT EXISTS last_error TEXT;
