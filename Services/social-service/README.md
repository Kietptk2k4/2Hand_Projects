# Social Service

Microservice nội dung xã hội của **2Hands**: bài viết, bình luận, feed, follow, tìm kiếm và đồng bộ trạng thái người dùng từ Auth/Admin.

Service chạy **Spring Boot 3.5** + **Java 21**, kiến trúc **Clean Architecture** (delivery → application → domain → infrastructure), **polyglot persistence** và **Outbox Pattern** cho event phát ra ngoài.

---

## Tổng quan kiến trúc

```mermaid
flowchart LR
  Client[Mobile / Web] --> API["REST /api/v1/social/*"]
  API --> App[Application layer]
  App --> Domain[Domain layer]
  Domain --> Infra[Infrastructure]
  Infra --> PG[(PostgreSQL)]
  Infra --> Mongo[(MongoDB)]
  Infra --> Redis[(Redis)]
  Infra --> MinIO[(MinIO - optional)]
  Infra --> Kafka{{Kafka - optional}}
  Auth[auth-service] -. JWT .-> API
  Admin[admin-service] -. admin.post.moderated .-> Kafka
  Kafka -. consume .-> App
  App -. outbox .-> PG
  PG -. publish worker .-> Kafka
```

| Thành phần | Vai trò |
|-----------|---------|
| **MongoDB** | `posts`, `comments`, `user_projections` — nội dung đọc nhiều, feed, search |
| **PostgreSQL** | `follows`, `post_likes`, `post_saves`, `comment_reaction`, `search_history`, `outbox_events`, `processed_domain_events` |
| **Redis** | Rate limit upload media, cache phụ trợ |
| **MinIO** | Presigned URL upload ảnh/video post (bật qua `SOCIAL_MINIO_ENABLED`) |
| **Kafka** | Consumer projection user + moderation; publisher outbox (tắt mặc định local) |

---

## API (đã triển khai)

Base path trong service: **`http://localhost:3002/api/v1/social`**

Mọi endpoint (trừ actuator health) yêu cầu **Bearer JWT** — cùng `JWT_ACCESS_SECRET` với `auth-service`.

Response envelope chuẩn 2Hands: `{ code, success, message, data, errors, timestamp }`.

### Feed

| Method | Path | Mô tả |
|--------|------|--------|
| `GET` | `/feed/global` | Feed công khai toàn hệ thống |
| `GET` | `/feed/following` | Feed từ người đang follow |

### Bài viết (`/posts`)

| Method | Path | Mô tả |
|--------|------|--------|
| `POST` | `/posts` | Tạo bài (caption, media, hashtag, product tag, visibility) |
| `PUT` | `/posts/{postId}` | Sửa bài (chủ sở hữu) |
| `DELETE` | `/posts/{postId}` | Xóa mềm bài |
| `GET` | `/posts/{postId}` | Chi tiết bài (visibility + moderation) |
| `GET` | `/posts/saved` | Danh sách bài đã lưu |
| `POST` | `/posts/media/upload-url` | Lấy presigned URL upload media |
| `POST` | `/posts/{postId}/like` | Like / unlike (toggle) |
| `GET` | `/posts/{postId}/likes` | Danh sách người đã like bài (phân trang) |
| `POST` | `/posts/{postId}/save` | Lưu / bỏ lưu (toggle) |
| `GET` | `/posts/{postId}/comments` | Danh sách comment (phân trang, sort, reply theo `parent_comment_id`) |
| `POST` | `/posts/{postId}/comments` | Tạo comment trên bài |

### Bình luận (`/comments`)

| Method | Path | Mô tả |
|--------|------|--------|
| `POST` | `/comments/{commentId}/replies` | Trả lời comment |
| `DELETE` | `/comments/{commentId}` | Xóa comment của mình |
| `POST` | `/comments/{commentId}/like` | Like comment |
| `GET` | `/comments/{commentId}/likes` | Danh sách người đã like comment (phân trang) |

### Người dùng xã hội (`/users`)

| Method | Path | Mô tả |
|--------|------|--------|
| `GET` | `/users/{userId}/profile` | Hồ sơ xã hội |
| `GET` | `/users/{userId}/posts` | Bài của user (`status_filter`: `published` \| `all`, owner-only với `all`) |
| `GET` | `/users/{userId}/relations` | Followers / following |
| `POST` | `/users/{userId}/follow` | Follow |
| `DELETE` | `/users/{userId}/follow` | Unfollow |
| `GET` | `/users/suggestions` | Gợi ý người dùng nên follow (phân trang) |

### Tìm kiếm (`/search`)

| Method | Path | Mô tả |
|--------|------|--------|
| `GET` | `/search/posts` | Tìm bài theo từ khóa |
| `GET` | `/search/hashtags/{hashtag}` | Tìm theo hashtag |
| `GET` | `/search/trending-hashtags` | Hashtag thịnh hành (engagement 7 ngày) |

> Qua API Gateway production, prefix thường là `/social-service/api/v1/social/...` — xem `docs/engineering_rules/api-standard.md`.

---

## Tích hợp sự kiện

### Tiêu thụ (Kafka consumer — `SOCIAL_KAFKA_CONSUMER_ENABLED=true`)

| Topic | Mục đích |
|-------|----------|
| `auth.user.created` / `updated` / `deleted` | Đồng bộ `user_projections` |
| `admin.user.suspended` / `banned` / `restricted` / `enforcement_revoked` / `enforcement_expired` | Cập nhật trạng thái enforcement trên projection |
| `admin.post.moderated` | Xử lý moderation (`HIDE` → ẩn khỏi feed; `REMOVE` → soft delete), idempotent qua `processed_domain_events` |

### Phát (Outbox — `SOCIAL_OUTBOX_PUBLISH_ENABLED=true`)

Ghi `outbox_events` trong cùng transaction ghi dữ liệu; scheduler publish lên Kafka:

| Event type | Topic |
|------------|--------|
| `POST_LIKED` | `social.post.liked` |
| `COMMENT_CREATED` | `social.comment.created` |
| `USER_FOLLOWED` | `social.user.followed` |

Scheduler retry outbox failed: `SOCIAL_OUTBOX_RETRY_ENABLED`.

---

## Chạy local

### 1. Hạ tầng (Docker)

Từ thư mục gốc monorepo:

```bash
cd Infrastructure
docker compose up -d postgres-social mongodb redis
```

Tùy chọn thêm MinIO (upload media) và Kafka (consumer/outbox):

```bash
docker compose up -d minio
# Kafka: cần broker riêng — chưa có trong docker-compose mặc định
```

| Dependency | Host mặc định |
|------------|----------------|
| PostgreSQL `social_db` | `localhost:5433` (user/pass: `postgres` / `123456`) |
| MongoDB `social_db` | `mongodb://localhost:27017/social_db` |
| Redis | `localhost:6379` |
| MinIO | `http://localhost:9000` (console `:9001`) |

Flyway chạy migration PostgreSQL khi khởi động. Index MongoDB tham chiếu `src/main/resources/db/migration/V1__init_social_mongo.js` (chạy thủ công / init nếu cần).

### 2. Biến môi trường

Tạo file `.env` trong `Services/social-service` (service dùng `spring-dotenv`):

```env
JWT_ACCESS_SECRET=<cùng auth-service, tối thiểu 32 ký tự>
JWT_REFRESH_SECRET=<cùng auth-service>

# Tùy chọn — override mặc định
DB_URL=jdbc:postgresql://localhost:5433/social_db
MONGO_URI=mongodb://localhost:27017/social_db
REDIS_HOST=localhost
REDIS_PORT=6379

SOCIAL_MINIO_ENABLED=false
SOCIAL_KAFKA_CONSUMER_ENABLED=false
SOCIAL_OUTBOX_PUBLISH_ENABLED=false
SOCIAL_OUTBOX_RETRY_ENABLED=false
```

### 3. Chạy service

```bash
cd Services/social-service
./gradlew bootRun
```

- **Port:** `3002`
- **Health (public):** `GET http://localhost:3002/actuator/health`
- **Timezone JVM:** UTC (`bootRun` set `user.timezone=UTC`); JSON Jackson: `Asia/Ho_Chi_Minh`

### 4. Smoke test nhanh

Lấy access token từ `auth-service`, sau đó:

```bash
curl -s http://localhost:3002/api/v1/social/feed/global?page=0&size=10 \
  -H "Authorization: Bearer <access_token>"
```

---

## Kiểm thử

```bash
cd Services/social-service
./gradlew test
```

- **Unit:** `src/test/java/.../unit/` — use case, parser, policy
- **Integration:** `src/test/java/.../integration/` — `@WebMvcTest` / API flow với mock infrastructure

Báo cáo HTML: `build/reports/tests/test/index.html`

---

## Cấu trúc mã nguồn

```
src/main/java/com/twohands/social_service/
├── application/          # Use cases, command/result, transaction boundary
├── delivery/http/        # Controllers, request/response, mappers
├── domain/               # Entities, repository interfaces, business rules
├── infrastructure/       # Mongo, JPA, Redis, Kafka, MinIO, outbox adapters
├── config/               # Security, Kafka, MinIO, scheduling
├── security/             # JWT filter, AuthenticatedUser
└── exception/            # AppException, ErrorCode, global handler
```

Quy ước chi tiết: `.cursor/rules/social/`, `docs/engineering_rules/backend-convention.md`.

---

## Tài liệu liên quan

| Tài liệu | Đường dẫn |
|----------|-----------|
| Business spec (MVP) | [`docs/business-spec/social-service-spec.md`](../../docs/business-spec/social-service-spec.md) |
| Feature requirements | [`docs/feature_requirements/social/`](../../docs/feature_requirements/social/) |
| API & FE behavior | [`docs/api_fe_behavior/social_api_fe_behavior/`](../../docs/api_fe_behavior/social_api_fe_behavior/) |
| Kiến trúc hệ thống | [`docs/architecture/system-architecture.md`](../../docs/architecture/system-architecture.md) |
| API envelope & lỗi | [`docs/engineering_rules/api-standard.md`](../../docs/engineering_rules/api-standard.md) |

---

## Cổng local trong monorepo

| Service | HTTP | PostgreSQL |
|---------|------|------------|
| auth-service | 3001 | 5432 |
| **social-service** | **3002** | **5433** |
| commerce-service | 3003 | 5434 |
| notification-service | — | 5435 |
| admin-service | 3004 | 5436 |

---

## Ghi chú vận hành

- **Moderation:** Bài `HIDDEN` không hiện với viewer thường (trả `404`); tác giả vẫn xem được chi tiết. Feed/search/profile lọc `moderation_status` khác `HIDDEN`.
- **Upload media:** Cần `SOCIAL_MINIO_ENABLED=true` và bucket `2hands-social-post` (hoặc override `MINIO_SOCIAL_POST_BUCKET`).
- **Kafka local:** Consumer và outbox publisher **tắt mặc định** — bật khi đã có broker và topic.
- **Chưa có trong MVP:** Outbox `POST_HIDDEN` / `POST_DELETED` sau moderation; Kafka chưa đóng gói trong `Infrastructure/docker-compose.yml`.
