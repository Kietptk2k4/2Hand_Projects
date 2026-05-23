# Log Critical Admin Action Payload – Internal & Behavior

## 1. Business Goal

Lưu payload đã sanitize cho hành động admin **critical** vào `admin_action_logs.request_payload` (và `result` trong `response_payload`) — đủ để audit quyết định, gồm before/after summary, không lưu secret.

## 2. API Contract

Internal only. Gọi qua:

- `LogCriticalAdminActionPayloadUseCase.execute(LogCriticalAdminActionPayloadCommand)`
- `AdminActionAuditLogger.logCritical(...)`

## 3. Payload Shape (sau sanitize)

```json
{
  "summary": "Changed max cart items",
  "before": { "config_key": "MAX_CART_ITEMS", "config_value": "20" },
  "after": { "config_key": "MAX_CART_ITEMS", "config_value": "30" },
  "context": { "reason": "capacity planning" }
}
```

`response_payload` envelope:

```json
{
  "status": "SUCCESS",
  "message": "Config updated",
  "result": {
    "result_summary": { "history_id": "uuid" }
  }
}
```

## 4. Business Rules

- Chỉ áp dụng khi `AdminActionLogPolicy.isCriticalAction(actionType)`.
- Redact: `password`, `token`, `otp`, `secret`, `authorization`, `cookie` (+ biến thể `access_token`, `refresh_token`, …).
- Max depth **4**, max **40** fields/object — tránh object graph lớn (`[MAX_DEPTH]`, `_truncated_fields`).
- Max JSON length **8000** chars → `...[TRUNCATED]`.
- Ghi **cùng transaction** với `LogAdminActionUseCase` (`@Transactional`).
- Serialization / empty payload → `ADMIN-400-AUDIT` và **fail** critical operation.
- Không lưu raw HTTP body.

## 5. Critical Action Types

Giống `AdminActionLogPolicy`: `USER_SUSPEND`, `USER_BAN`, `USER_RESTRICT`, `USER_ENFORCEMENT_REVOKE`, `PRODUCT_REMOVE`, `REVIEW_HIDE`, `SHOP_SUSPEND`, `SHOP_CLOSE`, `POST_MODERATE`, `COMMENT_MODERATE`, `SYSTEM_CONFIG_*`, `SYSTEM_ANNOUNCEMENT_PUBLISH`, `REFUND_EXECUTE`.

## 6. Edge Cases

- Payload rỗng (không summary/before/after/context) → reject.
- `ORDER_SUPPORT_VIEW` qua critical API → validation error.
- Nested object quá sâu → `[MAX_DEPTH]`.
- Đọc payload: [ViewAdminActionLogs](./ViewAdminActionLogs-api-and-behavior.md) với `ADMIN_AUDIT_VIEW`.

## 7. Usage Example

```java
adminActionAuditLogger.logCritical(
    adminId,
    "SYSTEM_CONFIG_UPDATE",
    AdminActionTargetType.CONFIG,
    configKey,
    AdminActionStatus.SUCCESS,
    "Config updated",
    "Changed max cart items",
    Map.of("config_key", configKey, "config_value", oldValue),
    Map.of("config_key", configKey, "config_value", newValue),
    Map.of("reason", reason),
    Map.of("history_id", historyId.toString())
);
```

## 8. Relation to FR_LogAdminAction

- `FR_LogAdminAction`: ghi log + IP/UA + status.
- `FR_LogCriticalAdminActionPayload`: quy tắc payload critical (before/after, redact, compact).
- Non-critical actions vẫn dùng `logSuccess()` không lưu `request_payload`.
