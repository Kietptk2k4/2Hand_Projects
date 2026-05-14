# Event-Driven Architecture (EDA) - 2Hands (MVP)

## 1. Tổng quan (Overview)
Hệ thống 2Hands áp dụng Event-Driven Architecture để giao tiếp giữa các Microservices. Việc này giúp giảm độ trễ của API, tăng tính sẵn sàng và đảm bảo tính nhất quán cuối cùng (Eventual Consistency) mà không cần dùng đến Distributed Transactions (2PC) vốn nặng nề.

## 2. Transactional Outbox Pattern
Để giải quyết bài toán "Dual-Write" (Vừa lưu data vào DB, vừa đẩy event lên Broker mà 1 trong 2 bị lỗi gây lệch data), 2Hands sử dụng **Outbox Pattern**.

### 2.1. Cấu trúc bảng `OUTBOX_EVENTS`
Mỗi Service (Auth, Social, Commerce) đều sở hữu một bảng `outbox_events` độc lập trong Database của nó:
* `id`: UUID
* `event_type`: Tên sự kiện (vd: `USER_CREATED`)
* `payload`: Dữ liệu JSON chứa thông tin sự kiện
* `status`: `PENDING` | `PROCESSING` | `COMPLETED` | `FAILED`
* `retry_count`: Số lần đã thử lại
* `created_at`, `processed_at`: Timestamps

### 2.2. Luồng hoạt động (Workflow)
1. **Local Transaction:** Khi có nghiệp vụ xảy ra (VD: Tạo Order), Commerce Service mở Transaction -> Lưu `ORDER` -> Lưu `OUTBOX_EVENTS` (trạng thái `PENDING`) -> Commit Transaction.
2. **Message Relay (Worker):** Một background worker liên tục quét (Polling) các record `PENDING` (hoặc dùng Change Data Capture - Debezium).
3. **Publish:** Đẩy event lên Message Broker (RabbitMQ/Kafka/PubSub).
4. **Mark Completed:** Nhận ACK từ Broker, cập nhật status thành `COMPLETED`.

## 3. Quản lý Lỗi & Retry (Failure Handling)
* **Retry Policy:** Worker sẽ retry các event `FAILED` hoặc kẹt ở `PROCESSING` quá thời gian quy định. Mỗi lần lỗi tăng `retry_count`.
* **Dead-letter / Permanent Failure:** Nếu `retry_count` vượt mức giới hạn (limit), đổi `status = FAILED`, ghi nhận `last_error`. (Trong MVP, dùng logic retry trên DB thay vì DLQ phức tạp).

## 4. Ma trận Sự kiện (Event Matrix Integration)

Dưới đây là các luồng Consume chính của **Notification Service** từ các Service khác:

| Từ Service | Loại Sự kiện (Event Type) | Hành động (Notification Action) |
| :--- | :--- | :--- |
| **Auth** | `USER_CREATED` | Gửi Email/Push Welcome |
| **Auth** | `EMAIL_VERIFICATION_REQUESTED` | Gửi Email chứa OTP Verify |
| **Auth** | `PASSWORD_CHANGED` | Gửi cảnh báo bảo mật tài khoản |
| **Social** | `POST_LIKED` | Push Notif: "Ai đó đã thích bài viết của bạn" |
| **Social** | `COMMENT_CREATED` | Push Notif: "Có bình luận mới trên bài viết" |
| **Social** | `USER_FOLLOWED` | Push Notif: "Bạn có người theo dõi mới" |
| **Commerce** | `ORDER_CREATED` | In-app/Push: Đặt hàng thành công, chờ thanh toán |
| **Commerce** | `PAYMENT_SUCCESS` | In-app/Push: Thanh toán thành công (payOS Webhook) |
| **Commerce** | `SHIPMENT_SHIPPED` | In-app/Push: Đơn hàng đang được giao |