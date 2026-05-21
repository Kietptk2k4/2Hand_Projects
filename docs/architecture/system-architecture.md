# System Architecture - Hệ thống 2Hands (MVP)

## 1. Tổng quan Kiến trúc (Architecture Overview)
Hệ thống 2Hands được thiết kế theo kiến trúc **Microservices**, phân tách các miền nghiệp vụ (business domains) thành các dịch vụ độc lập. Điều này giúp hệ thống dễ dàng mở rộng (scale), bảo trì và cho phép các team phát triển song song.

Hệ thống kết hợp giữa giao tiếp đồng bộ (Synchronous - RESTful APIs/gRPC) cho các thao tác yêu cầu phản hồi ngay lập tức, và giao tiếp bất đồng bộ (Asynchronous - Event-Driven) thông qua Message Broker để đảm bảo tính lỏng lẻo (loose coupling) giữa các services.

## 2. Các thành phần cốt lõi (Core Components)

### 2.1. API Gateway
* **Vai trò:** Điểm chạm duy nhất (Single Entry-point) cho toàn bộ Client (Web/Mobile App).
* **Nhiệm vụ:**
  * Routing (Định tuyến) request đến các Microservices tương ứng.
  * Authentication Interceptor (Xác thực JWT Token hợp lệ trước khi cho đi tiếp).
  * Rate Limiting & Throttling (Chống spam/DDoS).
  * Load Balancing.

### 2.2. Microservices
1. **Auth Service:** Quản lý tài khoản, xác thực (Email/OAuth), phân quyền (RBAC), và quản lý phiên làm việc (Session).
2. **Social Service:** Xử lý các luồng mạng xã hội bao gồm Bài viết (Post), Bình luận (Comment), Lượt thích (Like), Lưu bài (Save) và Theo dõi (Follow).
3. **Commerce Service:** Trái tim của E-commerce, quản lý Sản phẩm (Product), Giỏ hàng (Cart), Đơn hàng (Order), Thanh toán (Payment) và Vận chuyển (Shipping).
4. **Notification Service:** Consumer trung tâm, lắng nghe các sự kiện từ hệ thống và phát thông báo In-app, Email, Push Notifications.
5. **Admin Service:** Quản lý nội dung, kiểm duyệt (Moderation), và cấu hình hệ thống.

### 2.3. Hệ sinh thái bên thứ 3 (Third-party Integrations)
* **Payment Gateway:** Tích hợp **payOS** để tạo link thanh toán, xử lý Webhook trả về và tự động cập nhật trạng thái đơn hàng.
* **Logistics:** Tích hợp **GHN (Giao Hàng Nhanh)** để tạo mã vận đơn và theo dõi trạng thái giao hàng.
* **Identity Providers:** Google, Facebook cho tính năng đăng nhập OAuth.
* **Object Storage (MVP local):** **MinIO** shared (`Infrastructure/docker-compose.yml`, `:9000`). Auth avatar → bucket `2hands-avatar`; Commerce product/shop/review → `2hands-commerce-product`, `2hands-commerce-shop`, `2hands-commerce-review` (PostgreSQL chỉ lưu URL). Chi tiết: `docs/engineering_rules/commerce-object-storage.md`, `docs/business-spec/commerce-service-spec.md` § 1.1.
* **Cloud Storage (production option):** Amazon S3 hoặc CDN S3-compatible (cùng contract URL với MinIO dev); Social post images có thể dùng MongoDB + URL object storage tùy triển khai.

---

## 3. Sơ đồ Giao tiếp (Communication Flow)
* **Client -> API Gateway -> Service:** Giao tiếp qua HTTP/REST hoặc GraphQL.
* **Service <-> Service (Đồng bộ):** Sử dụng gRPC hoặc REST nội bộ (ví dụ: Commerce Service gọi Auth Service để check quyền đặc biệt). Tránh lạm dụng để không tạo điểm nghẽn (bottleneck).
* **Service -> Broker -> Service (Bất đồng bộ):** Sử dụng Outbox Pattern kết hợp RabbitMQ/Kafka (Chi tiết tại file Event-Driven Architecture).