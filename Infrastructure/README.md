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
| **Kafka** | `kafka` | **9092** | Broker — app trên host: `localhost:9092` |
| **Kafka UI** | `kafka-ui` | **8080** | Debug topic/message — http://localhost:8080 |

Chi tiết Kafka: [`docs/kafka/kafka_section_0.md`](../docs/kafka/kafka_section_0.md)
