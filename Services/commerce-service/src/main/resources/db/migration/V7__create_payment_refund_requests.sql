ALTER TYPE payment_status ADD VALUE IF NOT EXISTS 'REFUNDED';

CREATE TYPE payment_refund_request_status AS ENUM (
    'REQUESTED',
    'CONFIRMED',
    'REJECTED'
);

CREATE TYPE payment_refund_requested_by AS ENUM (
    'BUYER',
    'SELLER'
);

CREATE TABLE payment_refund_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id UUID NOT NULL REFERENCES payments(id),
    order_id UUID NOT NULL REFERENCES orders(id),
    requested_by payment_refund_requested_by NOT NULL,
    requested_by_user_id UUID NOT NULL,
    status payment_refund_request_status NOT NULL DEFAULT 'REQUESTED',
    amount NUMERIC NOT NULL CHECK (amount > 0),
    reason TEXT,
    admin_note TEXT,
    confirmed_at TIMESTAMP,
    rejected_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_payment_refund_requests_payment ON payment_refund_requests(payment_id);
CREATE INDEX idx_payment_refund_requests_order ON payment_refund_requests(order_id);
CREATE INDEX idx_payment_refund_requests_status ON payment_refund_requests(status);
CREATE INDEX idx_payment_refund_requests_created_at ON payment_refund_requests(created_at DESC);

CREATE UNIQUE INDEX uq_payment_refund_requests_active_order
    ON payment_refund_requests(order_id)
    WHERE status = 'REQUESTED';
