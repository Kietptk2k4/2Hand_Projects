# Notification Service Database Schema

Dịch vụ xử lý thông báo đa kênh. Sử dụng **PostgreSQL**.

## 1. NOTIFICATION_EVENTS
Queue nội bộ để xử lý các event nhận được từ Broker.
- `id`: UUID (PK)
- `event_type`: String
- `source_service`: String
- `payload`: JSONB
- `status`: Enum (PENDING, PROCESSING, COMPLETED, FAILED)
- `retry_count`: Integer
- `last_error`: Text
- `created_at`: Timestamp
- `processed_at`: Timestamp

## 2. USER_NOTIFICATIONS
Lưu trữ thông báo hiển thị cho người dùng (In-app).
- `id`: UUID (PK)
- `user_id`: UUID (Người nhận)
- `actor_id`: UUID (Người gây ra hành động - ví dụ người like bài)
- `type`: String (Phân loại UI)
- `title`, `content`: String/Text
- `reference_type`: String (POST, ORDER, etc.)
- `reference_id`: String
- `is_read`: BOOLEAN
- `is_deleted`: BOOLEAN
- `metadata`: JSONB (Deep link, thumbnail)
- `delivery_status`: Enum (PENDING, SENT, FAILED)
- `created_at`, `read_at`: Timestamp

## 3. USER_DEVICE_TOKENS
Quản lý FCM Token/Device Token cho Push Notification.
- `id`: UUID (PK)
- `user_id`: UUID
- `device_type`: Enum (IOS, ANDROID, WEB)
- `device_token`: String (UNIQUE)
- `is_active`: BOOLEAN
- `last_used_at`: Timestamp
- `created_at`, `updated_at`: Timestamp

## 4. USER_NOTIFICATION_SETTINGS
Cài đặt nhận thông báo theo từng loại của User.
- `user_id`: UUID
- `event_type`: String
- `allow_push`, `allow_email`, `allow_in_app`: BOOLEAN
- PRIMARY KEY (user_id, event_type)