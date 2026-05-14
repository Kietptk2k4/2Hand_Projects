# Admin Service Database Schema

Dịch vụ quản trị hệ thống và kiểm duyệt nội dung. Sử dụng **PostgreSQL**.

## 1. SYSTEM_CONFIGS
- `id`: UUID (PK)
- `config_key`: String (UNIQUE)
- `config_value`: JSONB
- `value_type`: Enum (INTEGER, DECIMAL, STRING, BOOLEAN, JSON)
- `description`: Text
- `is_active`: BOOLEAN
- `created_by`, `updated_by`: UUID
- `created_at`, `updated_at`: Timestamp

## 2. ADMIN_ACTION_LOGS
Audit trail các hành động của Admin.
- `id`: UUID (PK)
- `admin_id`: UUID
- `action_type`: Enum (USER_SUSPEND, PRODUCT_REMOVE, REVIEW_HIDE, REFUND_EXECUTE, etc.)
- `target_type`: String (USER, POST, PRODUCT, etc.)
- `target_id`: String
- `request_payload`, `response_payload`: JSONB (Nullable)
- `ip_address`, `user_agent`: String
- `created_at`: Timestamp

## 3. USER_ENFORCEMENTS
Lưu trữ các lệnh phạt đang áp dụng cho người dùng.
- `id`: UUID (PK)
- `user_id`: UUID
- `action_type`: Enum (BAN, SUSPEND, RESTRICT)
- `reason_code`: String
- `description`: Text
- `expires_at`: Timestamp (Nullable - if permanent)
- `enforced_by`: UUID (Admin ID)
- `status`: Enum (ACTIVE, REVOKED, EXPIRED)
- `created_at`, `updated_at`: Timestamp

## 4. CONTENT_MODERATION_LOGS
- `id`: UUID (PK)
- `target_type`: Enum (POST, COMMENT, PRODUCT, REVIEW)
- `target_id`: String
- `action`: Enum (HIDE, REMOVE, RESTORE)
- `reason`: Text
- `admin_id`: UUID
- `created_at`: Timestamp

## 5. SYSTEM_ANNOUNCEMENTS
Thông báo toàn hệ thống.
- `id`, `title`, `content`, `severity`, `is_pinned`, `status` (DRAFT, SENT, CANCELLED)

## 6. OUTBOX_EVENTS
- `id`, `event_type`, `payload`, `status`, `created_at`