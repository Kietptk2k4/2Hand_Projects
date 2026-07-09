# Infrastructure — Docker Compose (local dev)

Hạ tầng dùng chung cho các microservice 2Hands khi phát triển trên máy local (Windows/macOS/Linux).

## Chạy nhanh

```bash
cd Infrastructure
docker compose up -d
```

Chỉ Kafka + UI (hạng mục 0):

```bash
cd Infrastructure
docker compose up -d kafka kafka-ui
```

Kafka + MailHog (hạng mục 2B — email SMTP dev):

```bash
cd Infrastructure
docker compose up -d kafka kafka-ui mailhog
```

Web UI MailHog: http://localhost:8025 (SMTP `localhost:1025`).

Dừng:

```bash
docker compose down
```

## Port mapping

| Service | Container | Port host | Mục đích |
|---------|-----------|-----------|----------|
| PostgreSQL (auth) | `postgres-auth` | 5432 | `auth_db` |
| PostgreSQL (social) | `postgres-social` | 5433 | `social_db` |
| PostgreSQL (commerce) | `postgres-commerce` | 5434 | `commerce_db` |
| PostgreSQL (notification) | `postgres-notification` | 5435 | `notification_db` |
| PostgreSQL (admin) | `postgres-admin` | 5436 | `admin_db` |
| MongoDB | `mongodb` | 27017 | `social_db` (posts, comments) |
| Redis | `redis` | 6379 | Session, cache, rate limit |
| MinIO | `minio` | 9000 / 9001 | Object storage (API / console) |
| MinIO init | `minio-init` | — | Tạo bucket, CORS, public read (chạy một lần khi `up`) |
| **Kafka** | `kafka` | **9092** | Broker — app trên host: `localhost:9092` |
| **Kafka UI** | `kafka-ui` | **8080** | Debug topic/message — http://localhost:8080 |
| **MailHog** | `mailhog` | **1025** / **8025** | SMTP dev / Web UI — http://localhost:8025 |

Chi tiết Kafka: [`docs/kafka/kafka_section_0.md`](../docs/kafka/kafka_section_0.md) · Email SMTP dev: [`docs/kafka/kafka_section_2.md`](../docs/kafka/kafka_section_2.md) (2B)

## Application services + frontend (Docker)

Chạy **5 backend + frontend** trong Docker (không cần JDK/Gradle/Node trên host).

### Chuẩn bị env (một lần / máy mới)

Template **commit được**: `Services/*/.env.docker.example`, `frontend/.env.docker.example`.

```powershell
cd Infrastructure
./scripts/setup-docker-env.ps1
```

Tạo file gitignored `.env.docker`. Secret thật (OAuth, PayOS, VNPay, GHN): `Services/<service>/.env.docker.local`.

### Profile `apps` — JAR + nginx (onboarding)

```bash
cd Infrastructure
docker compose up -d
docker compose -f docker-compose.yml -f docker-compose.apps.yml --profile apps up -d --build
```

| URL | Mô tả |
|-----|--------|
| http://localhost:5173 | Frontend (nginx) |
| http://localhost:3001–3005 | auth, social, commerce, admin, notification |
| http://localhost:8025 | MailHog |
| http://localhost:8080 | Kafka UI |

### Profile `dev` — bootRun + Vite HMR

```bash
cd Infrastructure
docker compose -f docker-compose.yml -f docker-compose.dev.yml --profile dev up
```

- Backend: bind-mount source + `./gradlew bootRun`, JDWP cổng 5001–5005
- Frontend: Vite dev server cổng 5173
- **Không** chạy đồng thời `apps` và `dev` (trùng tên container)

### Luồng bootRun trên host (không Docker app)

Vẫn dùng `Services/*/.env` với host `localhost` — xem README từng service và mục “Chạy local nhanh” ở [README.md](../README.md).

## MinIO (local dev)

Khi `docker compose up -d`, service `minio-init` tự:

- Tạo bucket: `2hands-avatar`, `2hands-social-post`, `2hands-commerce-product`, `2hands-commerce-review`, `2hands-commerce-shop`
- CORS cluster-wide cho FE qua `MINIO_API_CORS_ALLOW_ORIGIN` trên service `minio` (community MinIO không hỗ trợ CORS per-bucket)
- Bật **anonymous download** trên các bucket (dev only) — `<img src="http://localhost:9000/...">` không bị 403

Console: http://localhost:9001 · API: http://localhost:9000 · Credentials: `admin` / `password123`

Chạy lại init sau khi đã có MinIO (bucket đã tồn tại vẫn an toàn):

```bash
cd Infrastructure
docker compose run --rm minio-init
```

Script: [`minio/init-buckets.sh`](minio/init-buckets.sh)

Sau khi đổi `MINIO_API_CORS_ALLOW_ORIGIN`, recreate container MinIO:

```bash
docker compose up -d --force-recreate minio
docker compose run --rm minio-init
```

## Remote demo — ngrok gateway (B1)

Expose **web + mobile + MinIO uploads + PayOS webhooks** through **one** public HTTPS URL via `dev-gateway` (nginx) and a single ngrok tunnel.

### Chuẩn bị (một lần)

1. Đăng ký ngrok, lấy authtoken: https://dashboard.ngrok.com/get-started/your-authtoken
2. Copy env:

```bash
cd Infrastructure
cp .env.example .env
# Sửa .env: NGROK_AUTHTOKEN=<token>
```

3. Copy ngrok service overrides (sau khi có URL từ bước chạy):

```bash
# Ví dụ auth — lặp cho social, commerce, admin, notification
cp ../Services/auth-service/.env.docker.ngrok.example ../Services/auth-service/.env.docker.local
# Sửa YOUR-NGROK-HOST trong .env.docker.local
```

Template checklist: [`.env.ngrok.example`](.env.ngrok.example) · Per-service: `Services/*/.env.docker.ngrok.example`

### Chạy (profile `dev` + `ngrok`)

**Khuyến nghị — một lệnh (orchestrate đủ 4 bước):**

```powershell
cd Infrastructure
./scripts/start-ngrok-demo.ps1
```

Script tự chạy theo thứ tự:

1. `docker compose ... --profile dev --profile ngrok up -d` — infra, backends, ngrok (gateway có thể trống lúc đầu)
2. Chờ tunnel HTTPS → `./scripts/print-ngrok-urls.ps1` — in URL + env checklist
3. `./scripts/build-frontend-ngrok.ps1` — build FE với `VITE_*` = URL ngrok hiện tại
4. `docker compose ... --force-recreate dev-gateway` — mount `frontend/dist` mới

Lần đầu (hoặc khi URL ngrok đổi): sau bước 2, sửa `Services/*/.env.docker.local` theo checklist rồi restart các service backend nếu cần (`docker compose ... restart auth-service social-service commerce-service admin-service notification-service`).

**Thủ công (từng bước):**

```powershell
cd Infrastructure

# 1. Stack + tunnel
docker compose -f docker-compose.yml -f docker-compose.dev.yml -f docker-compose.ngrok.yml --profile dev --profile ngrok up -d

# 2. URL + patch .env.docker.local
./scripts/print-ngrok-urls.ps1

# 3. Build FE (bắt buộc sau khi tunnel online)
./scripts/build-frontend-ngrok.ps1

# 4. Phục vụ dist mới
docker compose -f docker-compose.yml -f docker-compose.dev.yml -f docker-compose.ngrok.yml --profile dev --profile ngrok up -d --force-recreate dev-gateway
```

Không chạy `build-frontend-ngrok.ps1` trước khi tunnel online — URL bake vào bundle sẽ sai hoặc script sẽ fail.

### Lấy URL + env checklist

```powershell
cd Infrastructure
./scripts/print-ngrok-urls.ps1
./scripts/apply-ngrok-gateway-env.ps1
```

`apply-ngrok-gateway-env.ps1` cập nhật MinIO/CORS/OAuth env trên auth, social, commerce từ ngrok API (hoặc `-GatewayUrl`). **Không** ghi hostname vào DB — URL cũ `localhost:9000` được rewrite lúc API trả response.

```bash
sh scripts/print-ngrok-urls.sh
```

In ra: gateway HTTPS URL, PayOS webhook, `CORS_ALLOWED_ORIGINS`, OAuth redirects, MinIO presign, `VITE_*`, `EXPO_PUBLIC_*`.

### Env matrix (thay `{GATEWAY}` = `https://<ngrok-host>`)

| Biến | Service | Ghi chú |
|------|---------|---------|
| `CORS_ALLOWED_ORIGINS` | auth, social, commerce, admin, notification | `{GATEWAY}` |
| `AUTH_OAUTH2_SUCCESS_REDIRECT_URL` | auth | `{GATEWAY}/oauth/success` |
| `AUTH_OAUTH2_FAILURE_REDIRECT_URL` | auth | `{GATEWAY}/oauth/failure` |
| `AUTH_OAUTH2_COOKIE_SECURE` | auth | `true` |
| `SERVER_FORWARD_HEADERS_STRATEGY` | auth | `framework` (default in `application.yml`; required for OAuth `{baseUrl}` behind gateway) |
| Google OAuth redirect URI | Google Cloud Console | `{GATEWAY}/login/oauth2/code/google` |
| Facebook OAuth redirect URI | Meta Developer | `{GATEWAY}/login/oauth2/code/facebook` |
| `AUTH_MINIO_PRESIGNED_ENDPOINT` | auth | `{GATEWAY}` |
| `AUTH_MINIO_PUBLIC_URL` | auth | `{GATEWAY}/2hands-avatar` |
| `SOCIAL_MINIO_PRESIGNED_ENDPOINT` | social | `{GATEWAY}` |
| `SOCIAL_MINIO_PUBLIC_URL` | social | `{GATEWAY}/2hands-social-post` |
| `COMMERCE_MINIO_PRESIGNED_ENDPOINT` | commerce | `{GATEWAY}` |
| `COMMERCE_MINIO_PUBLIC_URL` | commerce | `{GATEWAY}` |
| `COMMERCE_PAYOS_RETURN_URL` | commerce | `{GATEWAY}/commerce/checkout/payment-result` |
| `COMMERCE_PAYOS_CANCEL_URL` | commerce | `{GATEWAY}/commerce/checkout/payment-result` |
| `VITE_*_SERVICE_BASE_URL` (×5) | frontend build | `{GATEWAY}` |
| `EXPO_PUBLIC_DEV_HOST` | mobile | host only (no `https://`) |
| `EXPO_PUBLIC_*_BASE_URL` | mobile | `{GATEWAY}` |

Upload ảnh qua gateway: client **không** gửi `client_upload_origin` trên host public; backend dùng `*_MINIO_PRESIGNED_ENDPOINT`.

### ngrok URL rotation (free tier)

URL đổi mỗi lần restart container `ngrok-gateway`:

1. `./scripts/print-ngrok-urls.ps1`
2. Cập nhật `Services/*/.env.docker.local` và `mobile/.env`
3. `./scripts/build-frontend-ngrok.ps1` + recreate `dev-gateway`
4. Cập nhật PayOS webhook trên my.payos.vn
5. Cập nhật Google/Facebook OAuth authorized redirect URIs (`{GATEWAY}/login/oauth2/code/{provider}`)

### Verification checklist

- [ ] `GET {GATEWAY}/api/v1/auth/...` (health/login) qua ngrok
- [ ] `GET {GATEWAY}/oauth2/authorization/google` redirect sang Google (không SPA 404)
- [ ] OAuth web: đăng nhập Google/Facebook → `/oauth/success` → session OK
- [ ] `GET {GATEWAY}/api/v1/social/feed/global` (JWT)
- [ ] `GET {GATEWAY}/commerce/api/v1/products` (public catalog)
- [ ] Browser: `{GATEWAY}/social`, `{GATEWAY}/commerce/checkout` (SPA)
- [ ] `GET {GATEWAY}/2hands-avatar/...` (MinIO proxy)
- [ ] Upload avatar/post/product qua presigned PUT
- [ ] `POST {GATEWAY}/commerce/api/v1/payments/webhooks/payos` reachable (PayOS)
- [ ] Local dev **không** bật `--profile ngrok` vẫn dùng `localhost` / LAN bình thường

| Service | Container | Port host | Mục đích |
|---------|-----------|-----------|----------|
| dev-gateway (profile `ngrok`) | `dev-gateway` | — (internal 8080) | nginx path router + FE static |
| ngrok (profile `ngrok`) | `ngrok-gateway` | 4040 | Tunnel + API inspect |

Config nginx: [`nginx/dev-gateway.conf`](nginx/dev-gateway.conf)

---

## PayOS webhook — ngrok (legacy, commerce-only)

> **Superseded:** dùng [Remote demo — ngrok gateway (B1)](#remote-demo--ngrok-gateway-b1) ở trên. Profile `payos` chỉ tunnel commerce `:3003` khi chưa dùng full gateway.

Tunnel HTTPS từ internet vào **commerce-service chạy trên host** (port `3003`). Dùng khi test webhook PayOS thật từ [my.payos.vn](https://my.payos.vn).

### Chuẩn bị (một lần)

1. Đăng ký ngrok, lấy authtoken: https://dashboard.ngrok.com/get-started/your-authtoken
2. Copy env:

```bash
cd Infrastructure
cp .env.example .env
# Sửa .env: NGROK_AUTHTOKEN=<token>
```

3. Cấu hình PayOS trong `Services/commerce-service/.env` (`COMMERCE_PAYOS_*`, return URL FE port `5173`).

### Chạy

```bash
# Terminal 1 — infra (+ ngrok nếu cần PayOS)
cd Infrastructure
docker compose -f docker-compose.yml -f docker-compose.payos.yml --profile payos up -d

# Chỉ bật ngrok khi infra đã chạy:
docker compose -f docker-compose.yml -f docker-compose.payos.yml --profile payos up -d ngrok-commerce

# Terminal 2 — commerce-service trên host (bắt buộc)
cd Services/commerce-service
./gradlew bootRun
```

### Lấy Webhook URL cho PayOS

```powershell
# Windows
cd Infrastructure
./scripts/print-payos-webhook-url.ps1
```

```bash
# macOS / Linux
cd Infrastructure
sh scripts/print-payos-webhook-url.sh
```

Dán URL in ra vào **Kênh thanh toán → Webhook URL** trên my.payos.vn:

`https://<ngrok-host>/commerce/api/v1/payments/webhooks/payos`

- ngrok dashboard (xem request webhook): http://localhost:4040
- Verify webhook dùng `COMMERCE_PAYOS_CHECKSUM_KEY` (không cần webhook secret riêng)
- Gói ngrok free: URL đổi mỗi lần restart container → cập nhật lại PayOS

| Service | Container | Port host | Mục đích |
|---------|-----------|-----------|----------|
| ngrok (profile `payos`) | `ngrok-commerce` | 4040 | Tunnel + API inspect |
