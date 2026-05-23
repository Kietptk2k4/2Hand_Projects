# Auth Service

Microservice **danh tính & truy cập** của 2Hands: đăng ký/đăng nhập, OAuth (Google/Facebook), JWT, RBAC, hồ sơ người dùng, enforcement snapshot và phát sự kiện user qua Outbox.

**Spring Boot 3.5** · **Java 21** · **PostgreSQL** + **Redis** · Clean Architecture (`delivery → application → domain → infrastructure`).

---

## Vai trò trong hệ thống

```mermaid
flowchart LR
  Client[Web / Mobile] --> Auth["auth-service :3001"]
  Auth --> PG[(PostgreSQL auth_db)]
  Auth --> Redis[(Redis — session / rate limit)]
  Auth --> MinIO[(MinIO — avatar, optional)]
  Auth -. outbox .-> Kafka{{Kafka — optional}}
  Kafka -. auth.user.* .-> Social[social-service]
  Kafka -. auth.* .-> Notif[notification-service]
  Admin[admin-service] -. enforcement events .-> Auth
```

| Thành phần | Vai trò |
|-----------|---------|
| **PostgreSQL** | Users, roles, permissions, sessions, enforcement snapshots, `outbox_events` |
| **Redis** | Refresh token / blacklist, rate limiting |
| **MinIO** | Presigned upload avatar (`2hands-avatar`) khi `AUTH_MINIO_ENABLED=true` |
| **OAuth2** | Google, Facebook — redirect về FE sau khi đăng nhập |

---

## API (đã triển khai)

Base URL local: **`http://localhost:3001`**

Response envelope: `{ code, success, message, data, errors, timestamp }` — lỗi prefix **`AUTH-*`**.

### Xác thực (`/api/v1/auth`)

| Method | Path | Auth | Mô tả |
|--------|------|------|--------|
| `POST` | `/register` | Public | Đăng ký email |
| `POST` | `/login` | Public | Đăng nhập → access + refresh token |
| `POST` | `/refresh` | Public | Làm mới access token |
| `POST` | `/logout` | Public | Thu hồi refresh token |
| `POST` | `/forgot-password` | Public | Yêu cầu reset mật khẩu |
| `POST` | `/change-password` | JWT | Đổi mật khẩu |
| `POST` | `/resend-email-verification` | Public | Gửi lại email xác minh |
| `POST` | `/admin/login` | Public | Đăng nhập admin (JWT + permissions) |
| `POST` | `/admin/logout` | JWT | Đăng xuất admin |
| `POST` | `/admin/token/refresh` | Public | Refresh token admin |

**OAuth2 (Spring Security):** `/oauth2/authorization/google` · `/oauth2/authorization/facebook` → callback `/login/oauth2/code/{provider}` → redirect FE (`AUTH_OAUTH2_SUCCESS_REDIRECT_URL`).

### Tài khoản (`/api/v1/users/me`) — JWT bắt buộc

| Method | Path | Mô tả |
|--------|------|--------|
| `GET` | `/` | Thông tin tài khoản |
| `PUT` | `/profile` | Cập nhật profile |
| `PATCH` | `/avatar` | Gán URL avatar sau upload |
| `POST` | `/avatar/upload-url` | Presigned URL upload |
| `PATCH` | `/privacy` | Cài đặt riêng tư |
| `PATCH` | `/settings` | Cài đặt người dùng |
| `GET` | `/sessions` | Phiên đăng nhập |
| `POST` | `/sessions/logout-all` | Đăng xuất mọi thiết bị |
| `GET` | `/login-history` | Lịch sử đăng nhập |
| `POST` | `/soft-delete` | Xóa mềm tài khoản |

### Công khai (`/api/v1/users`)

| Method | Path | Mô tả |
|--------|------|--------|
| `GET` | `/{userId}/public-profile` | Hồ sơ public (không cần JWT) |

### RBAC & điều tra admin (`/api/v1/admin`) — JWT + quyền admin

| Nhóm | Ví dụ endpoint |
|------|----------------|
| Roles | `GET /roles`, `GET /roles/{roleId}/permissions` |
| User permissions | `GET /users/{userId}/permissions`, `POST/DELETE .../roles` |
| Enforcement | `POST /users/{userId}/suspend|ban|restrict`, `POST /user-enforcements/{id}/revoke` |
| Investigation | `GET /users/{userId}/investigation-profile`, `/sessions`, `/login-history` |
| Sessions | `POST /sessions/{sessionId}/revoke` |

> Chi tiết contract: [`docs/api_fe_behavior/auth_api_fe_behavior/`](../../docs/api_fe_behavior/auth_api_fe_behavior/)

---

## Outbox (phát sự kiện)

Ghi `outbox_events` trong cùng transaction; publish khi `AUTH_OUTBOX_PUBLISH_ENABLED=true`.

| Event type | Topic |
|------------|--------|
| `USER_CREATED` | `auth.user.created` |
| `USER_UPDATED` | `auth.user.updated` |
| `USER_DELETED` | `auth.user.deleted` |
| `EMAIL_VERIFICATION_REQUESTED` | `auth.email.verification_requested` |
| `PASSWORD_RESET_REQUESTED` | `auth.password.reset_requested` |
| `PASSWORD_CHANGED` | `auth.password.changed` |

Worker: `PublishOutboxEventsUseCase`, `RetryFailedOutboxEventsUseCase` (scheduler tắt mặc định).

Auth còn **consume** enforcement events từ Admin (Kafka) qua `ConsumeUserEnforcementEventUseCase` khi consumer được bật.

---

## Chạy local

### 1. Hạ tầng

```bash
cd Infrastructure
docker compose up -d postgres-auth redis minio
```

| Dependency | Mặc định |
|------------|----------|
| PostgreSQL | `localhost:5432` / **`auth_db`** (user/pass: `postgres` / `123456`) |
| Redis | `localhost:6379` |
| MinIO | `localhost:9000` (tùy chọn avatar) |

> `application.yml` default JDBC có thể ghi `auth_service` — **nên set** `DB_URL=jdbc:postgresql://localhost:5432/auth_db` cho khớp Docker.

### 2. Biến môi trường (`.env` hoặc env shell)

```env
DB_URL=jdbc:postgresql://localhost:5432/auth_db
DB_USERNAME=postgres
DB_PASSWORD=123456

JWT_ACCESS_SECRET=<tối thiểu 32 ký tự — dùng chung các service>
JWT_REFRESH_SECRET=<tối thiểu 32 ký tự>

REDIS_HOST=localhost
REDIS_PORT=6379

# OAuth (bắt buộc nếu test OAuth)
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
FACEBOOK_CLIENT_ID=
FACEBOOK_CLIENT_SECRET=
AUTH_OAUTH2_SUCCESS_REDIRECT_URL=http://localhost:5173/oauth/success
AUTH_OAUTH2_FAILURE_REDIRECT_URL=http://localhost:5173/oauth/failure

AUTH_MINIO_ENABLED=false
AUTH_OUTBOX_PUBLISH_ENABLED=false
AUTH_OUTBOX_RETRY_ENABLED=false
```

### 3. Chạy service

```bash
cd Services/auth-service
./gradlew bootRun
```

- **Port:** `3001` (`SERVER_PORT` override được)
- **Health:** `GET http://localhost:3001/actuator/health`
- **Flyway:** migration tại `src/main/resources/db/migration/`

### 4. Smoke test

```bash
curl -s -X POST http://localhost:3001/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"your-password"}'
```

---

## Kiểm thử

```bash
cd Services/auth-service
./gradlew test
```

- Unit: `src/test/java/.../unit/`
- Integration: `src/test/java/.../integration/`

---

## Cấu trúc mã nguồn

```
src/main/java/com/twohands/auth_service/
├── application/     # Use cases (auth, useraccount, rbac, admin, outbox)
├── delivery/http/   # Controllers, DTO, mappers
├── domain/          # Entities, repository interfaces
├── infrastructure/  # JPA, Redis, OAuth, MinIO, outbox, Kafka
├── config/ security/ exception/
```

---

## Tài liệu

| Tài liệu | Đường dẫn |
|----------|-----------|
| Business spec | [`docs/business-spec/auth-service-spec.md`](../../docs/business-spec/auth-service-spec.md) |
| Feature requirements | [`docs/feature_requirements/auth/`](../../docs/feature_requirements/auth/) |
| API & FE behavior | [`docs/api_fe_behavior/auth_api_fe_behavior/`](../../docs/api_fe_behavior/auth_api_fe_behavior/) |
| DB schema | [`docs/database/auth-schema.md`](../../docs/database/auth-schema.md) |
| Monorepo | [`README.md`](../../README.md) |
