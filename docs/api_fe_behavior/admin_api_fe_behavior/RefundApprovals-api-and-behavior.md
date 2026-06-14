# Refund Approvals – API & Behavior

## 1. Business Goal

Admin duyet yeu cau hoan tien VNPay sau khi buyer/seller bam Huy don hang (PR5 queue). Admin hoan tien thu cong tren VNPay, roi bam Xac nhan da hoan tien de he thong REFUNDED + CANCELLED + release stock.

## 2. API Contract

Base (Admin gateway): /admin/api/v1/refund-approvals  
Commerce owner: /commerce/api/v1/admin/refund-approvals

| Method | Path | Permission |
|--------|------|------------|
| GET | /refund-approvals | REFUND_SUPPORT_READ |
| POST | /refund-approvals/{id}/confirm | REFUND_SUPPORT_APPROVE |
| POST | /refund-approvals/{id}/reject | REFUND_SUPPORT_APPROVE |

## 5. Business Rules

- Chi REQUESTED moi confirm/reject duoc.
- Confirm: payment REFUNDED, order CANCELLED, release inventory, outbox COMMERCE_PAYMENT_REFUNDED + COMMERCE_ORDER_CANCELLED.
- Reject: REJECTED, don tiep tuc PROCESSING.

## 6. FE Integration

- Admin: /admin?section=orderSupport&tab=refund-approvals
