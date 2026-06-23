ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS vnpay_frontend_return_url VARCHAR(512);
