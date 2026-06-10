CREATE TYPE seller_payout_request_status AS ENUM (
    'REQUESTED',
    'APPROVED',
    'PAID',
    'REJECTED',
    'CANCELLED'
);

CREATE TABLE seller_payout_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seller_id UUID NOT NULL,
    payout_account_id UUID NOT NULL REFERENCES seller_payout_accounts(id),
    amount NUMERIC NOT NULL CHECK (amount > 0),
    status seller_payout_request_status NOT NULL DEFAULT 'REQUESTED',
    admin_note TEXT,
    bank_transfer_ref VARCHAR(255),
    requested_at TIMESTAMP NOT NULL DEFAULT NOW(),
    approved_at TIMESTAMP,
    paid_at TIMESTAMP,
    rejected_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_seller_payout_requests_seller ON seller_payout_requests(seller_id);
CREATE INDEX idx_seller_payout_requests_status ON seller_payout_requests(status);
CREATE INDEX idx_seller_payout_requests_requested_at ON seller_payout_requests(requested_at DESC);

ALTER TABLE seller_ledger_entries
    ALTER COLUMN order_item_id DROP NOT NULL;

ALTER TABLE seller_ledger_entries
    ADD COLUMN payout_request_id UUID UNIQUE REFERENCES seller_payout_requests(id);
