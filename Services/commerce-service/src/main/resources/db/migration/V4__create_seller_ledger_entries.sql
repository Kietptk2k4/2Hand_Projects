CREATE TYPE seller_ledger_entry_type AS ENUM ('CREDIT', 'DEBIT');
CREATE TYPE seller_ledger_entry_status AS ENUM ('POSTED', 'REVERSED');

CREATE TABLE seller_ledger_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seller_id UUID NOT NULL,
    order_item_id UUID NOT NULL UNIQUE REFERENCES order_items(id),
    entry_type seller_ledger_entry_type NOT NULL,
    gross_amount NUMERIC NOT NULL CHECK (gross_amount >= 0),
    platform_fee_amount NUMERIC NOT NULL CHECK (platform_fee_amount >= 0),
    net_amount NUMERIC NOT NULL CHECK (net_amount >= 0),
    commission_rate_snapshot NUMERIC NOT NULL CHECK (
        commission_rate_snapshot >= 0 AND commission_rate_snapshot <= 1
    ),
    status seller_ledger_entry_status NOT NULL DEFAULT 'POSTED',
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_seller_ledger_entries_seller ON seller_ledger_entries(seller_id);
CREATE INDEX idx_seller_ledger_entries_seller_created ON seller_ledger_entries(seller_id, created_at DESC);

-- Backfill existing COMPLETED + PAID order items (default commission 10%).
INSERT INTO seller_ledger_entries (
    id,
    seller_id,
    order_item_id,
    entry_type,
    gross_amount,
    platform_fee_amount,
    net_amount,
    commission_rate_snapshot,
    status,
    created_at
)
SELECT gen_random_uuid(),
       oi.seller_id,
       oi.id,
       'CREDIT'::seller_ledger_entry_type,
       oi.final_price,
       ROUND(oi.final_price * 0.10, 0),
       oi.final_price - ROUND(oi.final_price * 0.10, 0),
       0.10,
       'POSTED'::seller_ledger_entry_status,
       COALESCE(oi.completed_at, oi.updated_at, NOW())
FROM order_items oi
INNER JOIN orders o ON o.id = oi.order_id
INNER JOIN payments p ON p.order_id = o.id
WHERE oi.status = 'COMPLETED'
  AND p.status = 'PAID'
ON CONFLICT (order_item_id) DO NOTHING;
