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

## PayOS webhook — ngrok (local dev)

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
