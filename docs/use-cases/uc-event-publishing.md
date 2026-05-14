# UC - Event Publishing (Transactional Outbox)

## 1. Overview
Đảm bảo tính nhất quán cuối cùng (Eventual Consistency) giữa các Microservices. Mọi thay đổi State của User (Tạo mới, Đổi Avatar, Đổi mật khẩu, Xóa) đều được phát tín hiệu ra ngoài thông qua Kafka/RabbitMQ.

## 2. Actors
* **Outbox Worker / Cron Job:** Background process của Auth Service.
* **Message Broker:** Hệ thống trung chuyển Kafka/RabbitMQ.

## 3. Sub-Use Cases

### 3.1. Publish Event (Standard Flow)
* **Pre-conditions:** Các logic nghiệp vụ (như tạo tài khoản) đã ghi bản ghi vào `OUTBOX_EVENTS` với trạng thái `PENDING`.
* **Main Flow:**
  1. Worker quét định kỳ (hoặc qua CDC) bảng `OUTBOX_EVENTS` lấy các bản ghi `PENDING`.
  2. Đổi trạng thái sang `PROCESSING`.
  3. Gửi payload lên Message Broker (ví dụ topic: `auth.user.created`).
  4. Nếu Broker phản hồi ACK thành công -> Cập nhật trạng thái thành `PUBLISHED`, set `published_at` = now().
* **Exception Flow:** Broker không phản hồi (Timeout) -> Update lại thành `PENDING`, tăng `retry_count`.

### 3.2. Retry Failed Outbox Events
* **Pre-conditions:** Các event gửi lỗi nhiều lần bị chuyển sang `FAILED` hoặc kẹt ở `PENDING`/`PROCESSING` quá lâu.
* **Main Flow:**
  1. Cron Job định kỳ (VD: mỗi 5 phút) quét các event `FAILED` hoặc `PENDING` bị timeout.
  2. Thử Publish lại lên Broker.
  3. Cập nhật `retry_count += 1`.
  4. Nếu `retry_count` vượt quá giới hạn (VD: 5 lần) -> Giữ nguyên `FAILED`, ghi log cảnh báo (Dead-letter trigger).