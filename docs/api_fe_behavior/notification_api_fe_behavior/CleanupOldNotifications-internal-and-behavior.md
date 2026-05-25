# Cleanup Old Notifications – Internal & Behavior

## 1. Business Goal

Job nền **optional** soft-delete notification cũ theo retention để kiểm soát dung lượng `user_notifications`. User không tương tác trực tiếp.

## 2. Trigger

- Scheduler: `CleanupOldNotificationsScheduler`
- Cron mặc định: `0 0 4 * * *` (hàng ngày 04:00)
- Bật: `NOTIFICATION_CLEANUP_OLD_NOTIFICATIONS_ENABLED=true`

## 3. Flow

1. Tính `createdBefore = now - retentionDays` (mặc định **180** ngày).
2. Lấy batch candidate: `is_deleted=false`, `created_at < cutoff`, không thuộc type/reference được giữ.
3. `UPDATE user_notifications SET is_deleted=true` theo batch.
4. Lặp tối đa `max-batches-per-run` (mặc định 100) hoặc đến khi hết candidate.

## 4. Retention policy (MVP)

**Soft delete** — không hard delete.

**Không cleanup:**

| Rule | Lý do |
|------|--------|
| `type` ∈ `USER_SUSPENDED`, `USER_RESTRICTED`, `SHOP_SUSPENDED`, `PASSWORD_CHANGED`, `SYSTEM_ANNOUNCEMENT_SENT` | Account/system critical |
| `reference_type = SYSTEM_ANNOUNCEMENT` | Announcement records |
| `created_at` trong window retention | Recent notifications |
| Đã `is_deleted=true` | Idempotent / user đã xóa |

## 5. Configuration

| Env / property | Default | Mô tả |
|----------------|---------|--------|
| `NOTIFICATION_CLEANUP_OLD_NOTIFICATIONS_ENABLED` | `false` | Bật job |
| `NOTIFICATION_CLEANUP_OLD_NOTIFICATIONS_CRON` | `0 0 4 * * *` | Lịch chạy |
| `NOTIFICATION_CLEANUP_OLD_NOTIFICATIONS_BATCH_SIZE` | `100` | Kích thước batch |
| `NOTIFICATION_CLEANUP_OLD_NOTIFICATIONS_RETENTION_DAYS` | `180` | Số ngày giữ |
| `NOTIFICATION_CLEANUP_OLD_NOTIFICATIONS_MAX_BATCHES_PER_RUN` | `100` | Cap batch mỗi lần chạy |

## 6. Failure / safety

| Case | Behavior |
|------|----------|
| `retention-days <= 0` | Không xóa gì (fail-safe, log WARN) |
| `batch-size <= 0` | Return 0 |
| DB timeout | Job kết thúc; lần chạy sau tiếp tục (idempotent) |
| Rerun job | Chỉ soft-delete record còn `is_deleted=false` |

## 7. Out of scope

- User API `DELETE` / `dismiss` (FR riêng).
- Hard delete / archive table.
- Cleanup `notification_events` (chỉ `user_notifications` trong FR này).

## 8. Related

- `FR_DeleteNotification` — user soft delete từng notification.
- `FR_CleanupInvalidDeviceToken` — pattern worker tương tự.
