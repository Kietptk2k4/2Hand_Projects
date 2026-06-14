-- VNPay payment method and txn reference columns
ALTER TYPE payment_method ADD VALUE IF NOT EXISTS 'VNPAY';

ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS vnpay_txn_ref VARCHAR(255),
    ADD COLUMN IF NOT EXISTS vnpay_transaction_no VARCHAR(255);

CREATE INDEX IF NOT EXISTS idx_payments_vnpay_txn_ref ON payments(vnpay_txn_ref);
