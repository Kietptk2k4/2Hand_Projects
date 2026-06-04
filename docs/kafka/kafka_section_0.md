# Kafka — Hạng mục 0: Hạ tầng local

Tài liệu mô tả broker Kafka dev chạy qua Docker Compose. Đây là **hạng mục 0** — chỉ hạ tầng và cấu hình môi trường; chưa bật publisher/consumer trong ứng dụng Java.

---

## Mục tiêu và phạm vi

### Làm (hạng mục 0)

- Broker Kafka KRaft single-node trong `Infrastructure/docker-compose.yml` (không Zookeeper).
- Kafka UI để xem cluster, topic, message.
- Auto-create topic dev (`KAFKA_AUTO_CREATE_TOPICS_ENABLE=true`).
- Ghi env mẫu `KAFKA_BOOTSTRAP_SERVERS=localhost:9092` cho các service.
- Tài liệu topic MVP tham chiếu từ code (`*OutboxTopicResolver`, `NotificationKafkaConsumerProperties`).

### Không làm (để hạng mục 1+)

- Không implement Kafka publisher Java (auth/commerce/admin vẫn dùng `LoggingOutboxEventPublisher` hoặc tương đương).
- Không bật `AUTH_OUTBOX_PUBLISH_ENABLED`, `SOCIAL_KAFKA_CONSUMER_ENABLED`, `NOTIFICATION_KAFKA_CONSUMER_ENABLED`, …
- Không thêm `spring-kafka` producer vào service chưa có.
- Không script tạo topic thủ công (dev dùng auto-create).
- Không SMTP / verify-email API.

---

## Docker Compose services

| Service | Image | Port host | Ghi chú |
|---------|-------|-----------|---------|
| `kafka` | `apache/kafka:3.7.2` | 9092 | KRaft broker + controller, volume `kafka_data` → `/var/lib/kafka/data` |
| `kafka-ui` | `provectuslabs/kafka-ui:latest` | 8080 | UI — http://localhost:8080 |

### Env quan trọng (broker)

| Biến | Giá trị | Ý nghĩa |
|------|---------|---------|
| `KAFKA_PROCESS_ROLES` | `broker,controller` | KRaft single node |
| `KAFKA_ADVERTISED_LISTENERS` | `PLAINTEXT_HOST://localhost:9092,PLAINTEXT://kafka:19092` | Host Windows/macOS/Linux → `localhost:9092`; client trong Docker network → `kafka:19092` |
| `KAFKA_AUTO_CREATE_TOPICS_ENABLE` | `true` | Topic tự tạo khi producer ghi lần đầu (hạng mục 1+) |
| `KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR` | `1` | Dev single broker |
| `KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR` | `1` | Dev single broker |
| `KAFKA_NUM_PARTITIONS` | `3` | Default partition khi auto-create |

**Lưu ý listener:** Image `apache/kafka` dùng hai listener PLAINTEXT:

- **`PLAINTEXT_HOST://localhost:9092`** — Spring Boot / CLI trên **host** (ngoài container).
- **`PLAINTEXT://kafka:19092`** — **kafka-ui** và container khác trong cùng Docker network.

Quy ước tên topic: `{service}.{domain}.{action}` (ví dụ `auth.user.created`). Map đầy đủ nằm trong các `*OutboxTopicResolver` — xem bảng dưới.

---

## Auto-create topic

Với `KAFKA_AUTO_CREATE_TOPICS_ENABLE=true`:

1. Topic **chưa tồn tại** cho đến khi có producer ghi message lần đầu (hạng mục 1 — outbox worker publish lên Kafka).
2. Kafka tạo topic với `num.partitions=3`, replication factor theo broker (dev = 1).
3. Consumer (hạng mục 1+) subscribe topic đã tồn tại hoặc chờ producer tạo trước.

Ở hạng mục 0, danh sách topic trong Kafka UI thường **trống** — điều này bình thường.

---

## Bảng topic MVP (tham chiếu code)

### Auth — `AuthOutboxTopicResolver`

| Event type | Topic |
|------------|-------|
| `USER_CREATED` | `auth.user.created` |
| `USER_UPDATED` | `auth.user.updated` |
| `USER_DELETED` | `auth.user.deleted` |
| `EMAIL_VERIFICATION_REQUESTED` | `auth.email.verification_requested` |
| `PASSWORD_RESET_REQUESTED` | `auth.password.reset_requested` |
| `PASSWORD_CHANGED` | `auth.password.changed` |

### Social — `SocialOutboxTopicResolver` (publish)

| Event type | Topic |
|------------|-------|
| `POST_LIKED` | `social.post.liked` |
| `COMMENT_CREATED` | `social.comment.created` |
| `USER_FOLLOWED` | `social.user.followed` |

Social consumer (`SocialKafkaConsumerProperties`): `auth.user.created`, `auth.user.updated`, `auth.user.deleted`.

### Commerce — `CommerceOutboxTopicResolver`

| Event type | Topic |
|------------|-------|
| `COMMERCE_ORDER_CREATED` | `commerce.order.created` |
| `COMMERCE_SELLER_ORDER_ITEM_PROCESSING` | `commerce.seller_order_item.processing` |
| `COMMERCE_ORDER_CANCELLED` | `commerce.order.cancelled` |
| `COMMERCE_ORDER_COMPLETED` | `commerce.order.completed` |
| `COMMERCE_PAYMENT_CREATED` | `commerce.payment.created` |
| `COMMERCE_PAYMENT_PAID` | `commerce.payment.paid` |
| `COMMERCE_PAYMENT_FAILED` | `commerce.payment.failed` |
| `COMMERCE_PAYMENT_CANCELLED` | `commerce.payment.cancelled` |
| `COMMERCE_PAYMENT_EXPIRED` | `commerce.payment.expired` |
| `COMMERCE_SHIPMENT_CREATED` | `commerce.shipment.created` |
| `COMMERCE_SHIPMENT_STATUS_CHANGED` | `commerce.shipment.status_changed` |
| `COMMERCE_SHIPMENT_SHIPPED` | `commerce.shipment.shipped` |
| `COMMERCE_SHIPMENT_DELIVERED` | `commerce.shipment.delivered` |
| `COMMERCE_INVENTORY_RESERVED` | `commerce.inventory.reserved` |
| `COMMERCE_INVENTORY_RELEASED` | `commerce.inventory.released` |
| `COMMERCE_PRODUCT_*` | `commerce.product.*` (created, updated, published, …) |
| `COMMERCE_REVIEW_*` | `commerce.review.*` |
| `COMMERCE_SHOP_*` | `commerce.shop.*` |

*(Danh sách đầy đủ trong source: `CommerceOutboxTopicResolver.java`.)*

### Admin — `AdminOutboxTopicResolver`

| Event type | Topic |
|------------|-------|
| `USER_SUSPENDED` | `admin.user.suspended` |
| `USER_BANNED` | `admin.user.banned` |
| `USER_RESTRICTED` | `admin.user.restricted` |
| `USER_ENFORCEMENT_REVOKED` | `admin.user.enforcement_revoked` |
| `USER_ENFORCEMENT_EXPIRED` | `admin.user.enforcement_expired` |
| `PRODUCT_REMOVED` | `admin.product.removed` |
| `PRODUCT_RESTORED` | `admin.product.restored` |
| `REVIEW_HIDDEN` | `admin.review.hidden` |
| `REVIEW_REMOVED` | `admin.review.removed` |
| `REVIEW_RESTORED` | `admin.review.restored` |
| `SHOP_SUSPENDED` | `admin.shop.suspended` |
| `SHOP_RESTORED` | `admin.shop.restored` |
| `SHOP_CLOSED` | `admin.shop.closed` |
| `POST_MODERATED` | `admin.post.moderated` |
| `POST_RESTORED` | `admin.post.restored` |
| `COMMENT_MODERATED` | `admin.comment.moderated` |
| `COMMENT_RESTORED` | `admin.comment.restored` |
| `SYSTEM_CONFIG_UPDATED` | `admin.config.updated` |
| `SYSTEM_ANNOUNCEMENT_PUBLISHED` | `admin.announcement.published` |
| `SYSTEM_ANNOUNCEMENT_CANCELLED` | `admin.announcement.cancelled` |

### Notification — `NotificationKafkaConsumerProperties` (consume, hạng mục 1+)

Topics mặc định khi bật consumer:

`auth.user.created`, `auth.user.updated`, `auth.user.deleted`, `auth.email.verification_requested`, `auth.password.reset_requested`, `social.post.liked`, `social.comment.created`, `social.comment.replied`, `social.comment.liked`, `social.user.followed`, `commerce.order.created`, `commerce.payment.paid`, `commerce.payment.failed`, `commerce.shipment.created`, `commerce.shipment.shipped`, `commerce.shipment.delivered`, `commerce.order.completed`, `commerce.review.reminder`, `admin.user.suspended`, `admin.user.banned`, `admin.user.restricted`, `admin.product.removed`, `admin.review.hidden`, `admin.shop.suspended`, `admin.announcement.published`.

---

## Biến môi trường dev (`.env.example`)

| Service | Biến | Giá trị gợi ý |
|---------|------|----------------|
| social-service | `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` |
| notification-service | `NOTIFICATION_KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` |
| notification-service | `NOTIFICATION_KAFKA_CONSUMER_ENABLED` | **`false`** (hạng mục 0) |

Các flag publish/consume khác giữ **`false`** cho đến hạng mục 1.

---

## Lệnh chạy và verify

### 1. Khởi động Kafka

```bash
cd Infrastructure
docker compose up -d kafka kafka-ui
```

### 2. Kiểm tra container

```bash
docker compose ps kafka kafka-ui
docker compose logs kafka --tail 30
```

Broker sẵn sàng khi log có dạng `Kafka Server started` / `[KafkaRaftServer]`.

### 3. Kiểm tra port trên host (Windows PowerShell)

```powershell
Test-NetConnection localhost -Port 9092
```

`TcpTestSucceeded : True` → host có thể kết nối broker.

### 4. Kafka UI

Mở trình duyệt: **http://localhost:8080**

- Cluster **local** phải online (bootstrap nội bộ: `kafka:19092`).
- Tab **Brokers** hiển thị 1 broker; tab **Topics** có thể trống ở hạng mục 0.

### 5. (Tuỳ chọn) Smoke test CLI trong container

```bash
docker exec -it kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list

docker exec -it kafka /opt/kafka/bin/kafka-console-producer.sh \
  --bootstrap-server localhost:9092 --topic dev.smoke.test

docker exec -it kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 --topic dev.smoke.test --from-beginning
```

Topic `dev.smoke.test` sẽ xuất hiện trong UI sau lệnh producer.

---

## Việc chưa làm (hạng mục 2+)

| Hạng mục | Nội dung |
|----------|----------|
| 2 | Bật consumer Social / Notification |
| 2+ | SMTP, email verification flow end-to-end qua Kafka |
| 2+ | Verify-email API + event `EMAIL_VERIFICATION_REQUESTED` consume |

**Hạng mục 1 (outbox publisher):** đã triển khai — xem [kafka_section_1.md](kafka_section_1.md).

---

## Tài liệu liên quan

- [`Infrastructure/docker-compose.yml`](../../Infrastructure/docker-compose.yml)
- [`Infrastructure/README.md`](../../Infrastructure/README.md)
- [`docs/architecture/event-driven-architecture.md`](../architecture/event-driven-architecture.md)
