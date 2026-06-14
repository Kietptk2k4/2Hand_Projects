-- V4: admin audit action types for refund approval queue

ALTER TYPE admin_action_type ADD VALUE IF NOT EXISTS 'REFUND_SUPPORT_VIEW';
ALTER TYPE admin_action_type ADD VALUE IF NOT EXISTS 'REFUND_REQUEST_REJECT';
