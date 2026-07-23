# Refund Approvals – API & Behavior

## 1. Business Goal

Admin duyệt yêu cầu hoàn tiền VNPay sau khi buyer/seller bấm hủy đơn. Admin hoàn tiền thủ công trên VNPay, rồi xác nhận để hệ thống REFUNDED + CANCELLED + release stock.

## 2. API Contract

Base (Admin gateway): `/admin/api/v1/refund-approvals`  
Commerce owner: `/commerce/api/v1/admin/refund-approvals`

| Method | Path | Permission |
|--------|------|------------|
| GET | `/refund-approvals` | `REFUND_SUPPORT_READ` |
| GET | `/refund-approvals/{id}` | `REFUND_SUPPORT_READ` |
| POST | `/refund-approvals/{id}/confirm` | `REFUND_SUPPORT_APPROVE` |
| POST | `/refund-approvals/{id}/reject` | `REFUND_SUPPORT_APPROVE` |

### List query parameters

| Param | Type | Mô tả |
|-------|------|--------|
| `status` | string | `REQUESTED`, `CONFIRMED`, `REJECTED` |
| `q` | string | Tìm `refund_request_id`, `payment_id`, `order_id` (ILIKE) |
| `requested_by` | string | `BUYER`, `SELLER` |
| `payment_method` | string | `COD`, `PAYOS`, `VNPAY` |
| `from` | ISO-8601 / date | `created_at >= from` |
| `to` | ISO-8601 / date | `created_at <= to` |
| `page` | int | Trang (1-based), default `1` |
| `limit` | int | Kích thước trang, default `20`, max `50` |

**Success (200) list:**

```json
{
  "code": 200,
  "success": true,
  "message": "Refund approval queue retrieved successfully",
  "data": {
    "items": [],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total_items": 0,
      "total_pages": 1,
      "has_next": false
    }
  }
}
```

### Confirm / reject body

```json
{ "admin_note": "optional string" }
```

## 3. Business Rules

- Chỉ `REQUESTED` mới confirm/reject được.
- Confirm: payment `REFUNDED`, order `CANCELLED`, release inventory, outbox events.
- Reject: `REJECTED`, đơn tiếp tục `PROCESSING`.
- Ghi audit `REFUND_SUPPORT_VIEW` (list), `REFUND_EXECUTE` / `REFUND_REQUEST_REJECT` (actions).

## 4. FE Integration

- Route: `/admin?section=orderSupport&tab=refund-approvals`
- URL params: `refundRequestId`, `refundView=summary|detail|note`, `ref_*` filters
- MSW: `commerceAdminRefundApprovalHandlers.js`

## 5. Related

- Order detail `active_refund_request`: [ViewOrderSupportDetail-api-and-behavior.md](./ViewOrderSupportDetail-api-and-behavior.md)
