# Auth Service Database Schema

Dịch vụ quản lý định danh, xác thực và phân quyền cho hệ thống 2Hands. Sử dụng **PostgreSQL**.

## 1. USERS
Bảng lưu trữ thông tin tài khoản chính.
- `id`: UUID (Primary Key)
- `email`: String
- `email_normalized`: String (UNIQUE) - Dùng để login/search
- `phone`: String
- `phone_normalized`: String (UNIQUE)
- `password_hash`: String
- `status`: Enum (PENDING_VERIFICATION, ACTIVE, SUSPENDED, DELETED)
- `email_verified`: BOOLEAN (Default: false)
- `phone_verified`: BOOLEAN (Default: false)
- `last_login_at`: Timestamp
- `password_changed_at`: Timestamp
- `deleted_at`: Timestamp (Nullable - dùng cho Soft Delete)
- `created_at`: Timestamp
- `updated_at`: Timestamp

## 2. ROLES
- `id`: UUID (PK)
- `code`: String (UNIQUE)
- `name`: String
- `created_at`: Timestamp
- `updated_at`: Timestamp

## 3. PERMISSIONS
- `id`: UUID (PK)
- `code`: String (UNIQUE)
- `description`: String
- `created_at`: Timestamp
- `updated_at`: Timestamp

## 4. USER_ROLES
Bảng trung gian mapping User - Role.
- `user_id`: UUID (FK -> USERS)
- `role_id`: UUID (FK -> ROLES)
- UNIQUE(user_id, role_id)

## 5. ROLE_PERMISSIONS
Bảng trung gian mapping Role - Permission.
- `role_id`: UUID (FK -> ROLES)
- `permission_id`: UUID (FK -> PERMISSIONS)
- `created_at`: Timestamp
- `updated_at`: Timestamp

## 6. USER_PROFILES
Lưu trữ thông tin cá nhân mở rộng.
- `user_id`: UUID (PK, FK -> USERS)
- `display_name`: String
- `avatar_url`: String
- `bio`: Text
- `website`: String
- `social_links`: JSONB
- `is_private`: BOOLEAN
- `created_at`: Timestamp
- `updated_at`: Timestamp

## 7. USER_SETTINGS
- `user_id`: UUID (PK, FK -> USERS)
- `appearance_mode`: String
- `created_at`: Timestamp
- `updated_at`: Timestamp

## 8. LOGIN_LOGS
- `id`: UUID (PK)
- `user_id`: UUID (FK -> USERS)
- `login_method`: String (EMAIL, GOOGLE, FACEBOOK)
- `ip_address`: String
- `user_agent`: String
- `success`: BOOLEAN
- `created_at`: Timestamp

## 9. OAUTH_ACCOUNTS
- `id`: UUID (PK)
- `user_id`: UUID (FK -> USERS)
- `provider`: String
- `provider_user_id`: String
- `email`: String
- `created_at`: Timestamp
- `updated_at`: Timestamp

## 10. REFRESH_TOKEN_SESSION
- `id`: UUID (PK)
- `user_id`: UUID (FK -> USERS)
- `token_hash`: String
- `device_id`: String
- `ip_address`: String
- `user_agent`: String
- `expires_at`: Timestamp
- `status`: Enum (ACTIVE, EXPIRED, REVOKED, LOGGED_OUT)
- `created_at`: Timestamp
- `updated_at`: Timestamp

## 11. VERIFICATION_TOKENS
Lưu trữ OTP/Token cho verify email hoặc reset mật khẩu.
- `id`: UUID (PK)
- `user_id`: UUID (FK -> USERS)
- `token_hash`: String
- `type`: Enum (EMAIL_VERIFY, PASSWORD_RESET)
- `expires_at`: Timestamp
- `used_at`: Timestamp (Nullable)
- `created_at`: Timestamp

## 12. OUTBOX_EVENTS
Transactional Outbox cho Event-Driven.
- `id`: UUID (PK)
- `event_type`: String
- `source`: String
- `payload`: JSONB
- `status`: Enum (PENDING, PROCESSING, PUBLISHED, FAILED)
- `retry_count`: Integer
- `created_at`: Timestamp
- `published_at`: Timestamp
- `last_error`: Text