# Log Admin Action – Internal & Behavior

## 1. Business Goal

Ghi mọi hành động quản trị quan trọng vào `admin_action_logs` để audit, điều tra và compliance. Đây là **internal application concern** — không có public API riêng.

## 2. API Contract

Không expose REST endpoint. Các use case moderation/enforcement/config gọi:

- `LogAdminActionUseCase.execute(LogAdminActionCommand)`
- `AdminActionAuditLogger.logSuccess(...)` / `logFailure(...)` / `logAccessDenied(...)`

## 3. Persistence (`admin_action_logs`)

| Field | Mô tả |
|-------|--------|
| `admin_id` | Actor (UUID từ JWT) |
| `action_type` | Enum `admin_action_type` |
| `target_type` | `USER`, `PRODUCT`, `CONFIG`, `SECURITY`, … |
| `target_id` | Id đích |
| `request_payload` | JSONB — chỉ critical actions, đã redact |
| `response_payload` | JSONB — luôn có `status`: `SUCCESS` \| `FAILURE` |
| `ip_address`, `user_agent` | Từ HTTP request khi có |
| `created_at` | Append-only |

## 4. Business Rules

- Critical actions (enforcement, moderation, config, announcement, …) lưu `request_payload` đã sanitize.
- Non-critical (vd. `ORDER_SUPPORT_VIEW`) chỉ lưu envelope `status` trong `response_payload`.
- Ghi **cùng transaction** với domain write (`@Transactional` trên use case gọi logger).
- Access denied (403): log riêng `ADMIN_ACCESS_DENIED` với `REQUIRES_NEW` qua `AdminAccessDeniedAuditAspect`.
- Không log password, token, OTP, secret, authorization, cookie.
- Payload > 8KB → truncate.
- Audit write failure → fail operation (JPA propagate).

## 5. Critical Action Types

`USER_SUSPEND`, `USER_BAN`, `USER_RESTRICT`, `USER_ENFORCEMENT_REVOKE`, `PRODUCT_REMOVE`, `REVIEW_HIDE`, `SHOP_SUSPEND`, `SHOP_CLOSE`, `POST_MODERATE`, `COMMENT_MODERATE`, `SYSTEM_CONFIG_*`, `SYSTEM_ANNOUNCEMENT_PUBLISH`, `REFUND_EXECUTE`

## 6. Response Payload Envelope

```json
{
  "status": "SUCCESS",
  "message": "User suspended",
  "result": { }
}
```

## 7. Edge Cases

- Không có HTTP request (scheduler) → `ip_address` / `user_agent` null.
- `logAccessDenied` khi aspect không lấy được admin → skip (warn log).
- Enum mới `ADMIN_ACCESS_DENIED` (Flyway V2) cho security audit.

## 8. FE Integration Notes

- FE không gọi API log trực tiếp.
- Đọc logs: [ViewAdminActionLogs](./ViewAdminActionLogs-api-and-behavior.md) với permission `ADMIN_AUDIT_VIEW`.

## 9. Usage Example (backend)

```java
adminActionAuditLogger.logSuccess(
    adminId,
    "USER_SUSPEND",
    AdminActionTargetType.USER,
    userId.toString(),
    "User suspended",
    Map.of("reason", reasonCode, "description", description),
    Map.of("enforcementId", enforcementId.toString())
);
```
