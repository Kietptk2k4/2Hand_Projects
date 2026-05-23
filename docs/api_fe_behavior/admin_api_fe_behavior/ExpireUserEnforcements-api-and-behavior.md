# Expire User Enforcements – API & Behavior

## 1. Business Goal

Background job tự động hết hạn enforcement tạm thời khi `expires_at <= now`: cập nhật `EXPIRED`, ghi transition log (actor `SYSTEM`), publish `USER_ENFORCEMENT_EXPIRED` qua outbox để Auth/Social/Commerce gỡ hạn chế.

## 2. API Contract

**Internal scheduled job** — không có endpoint FE.

### Enforcement expiration scheduler

| Config key | Default | Mô tả |
|------------|---------|-------|
| `admin.jobs.enforcement-expiration.enabled` | `false` | Bật/tắt job. |
| `admin.jobs.enforcement-expiration.cron` | `0 0 * * * *` | Lịch chạy (mỗi giờ). |
| `admin.jobs.enforcement-expiration.batch-size` | `100` | Số enforcement tối đa mỗi lần poll. |

Env override: `ADMIN_ENFORCEMENT_EXPIRATION_ENABLED`, `ADMIN_ENFORCEMENT_EXPIRATION_CRON`, `ADMIN_ENFORCEMENT_EXPIRATION_BATCH_SIZE`.

## 3. Selection criteria

```text
status = ACTIVE
expires_at IS NOT NULL
expires_at <= now
```

- Enforcement vĩnh viễn (`expires_at = null`) **không** auto-expire.
- `REVOKED` / `EXPIRED` không được pick lại (idempotent).

Claim batch: `ORDER BY expires_at ASC`, `FOR UPDATE SKIP LOCKED` (an toàn khi nhiều worker).

## 4. Processing (mỗi enforcement trong một transaction batch)

1. Claim rows eligible.
2. Re-check `ACTIVE` + `expires_at <= now` (no-op nếu revoke thắng race).
3. `user_enforcements.status` → `EXPIRED`, `updated_at` = now.
4. `user_enforcement_logs`: `ACTIVE` → `EXPIRED`, `admin_id` = null, note = `Enforcement expired`.
5. Outbox `USER_ENFORCEMENT_EXPIRED` → topic `admin.user.enforcement_expired`.

**Không** gọi Auth HTTP trực tiếp; consumer xử lý async (giống revoke khi integration tắt).

### Outbox payload (ví dụ)

```json
{
  "user_id": "uuid",
  "enforcement_id": "uuid",
  "action_type": "SUSPEND",
  "reason_code": "POLICY",
  "previous_status": "ACTIVE",
  "new_status": "EXPIRED",
  "expires_at": "2026-05-20T10:00:00Z",
  "expired_at": "2026-05-20T10:05:00Z"
}
```

## 5. Response – Success

Quan sát qua log (không có HTTP body):

```
User enforcement expired. enforcementId=..., userId=..., actionType=SUSPEND, outboxEventId=...
User enforcement expiration job completed. expiredCount=3
Enforcement expiration scheduler completed. expiredCount=3
```

## 6. Edge cases

| Case | Hành vi |
|------|---------|
| Job chạy lại sau khi đã expire | `expiredCount=0`, không duplicate outbox |
| Admin revoke trước khi job chạy | Row không còn `ACTIVE` → không expire |
| `expires_at` vẫn trong tương lai | Không pick |
| Scheduler disabled | Không chạy; enforcement vẫn `ACTIVE` (UI có thể `possibly_expired` từ view current) |
| Partial batch failure | Transaction rollback cho batch; lần chạy sau retry các row còn eligible |

## 7. Related

| API / job | Mục đích |
|-----------|----------|
| `GET .../enforcements/current` | Moderation — có `possibly_expired` khi quá `expires_at` nhưng status vẫn ACTIVE |
| `POST .../user-enforcements/{id}/revoke` | Revoke thủ công |
| `PublishAdminEventsUseCase` / `RetryAdminOutboxEventsUseCase` | Publish outbox ra broker |

## 8. FE note

Không tích hợp trực tiếp. Timeline investigation dùng `GET .../enforcements/history` sau khi job đã chạy (status `EXPIRED`, log `SYSTEM`).
