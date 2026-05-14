# Database Strategy - 2Hands (MVP)

## 1. Nguyên tắc (Database per Service)
Đảm bảo tính độc lập tuyệt đối, **mỗi Microservice sở hữu một Database riêng biệt**. Không có chuyện Service A truy vấn trực tiếp vào Database của Service B. Mọi dữ liệu chia sẻ phải thông qua API (cho read) hoặc Event (cho replication/write).

## 2. Polyglot Persistence (Đa dạng lưu trữ)
Hệ thống sử dụng các loại Database khác nhau phù hợp với đặc thù nghiệp vụ:

### 2.1. Relational Database (PostgreSQL)
Được sử dụng cho các Service đòi hỏi tính toàn vẹn dữ liệu cao, cấu trúc tĩnh và quan hệ phức tạp (ACID transactions).
* **Auth Service DB:** Quản lý `USERS`, `ROLES`, `PERMISSIONS`, `USER_PROFILES`.
* **Commerce Service DB:** Quản lý `PRODUCTS`, `ORDERS`, `ORDER_ITEMS`, `PAYMENTS`, kho hàng. (Yêu cầu strict transaction khi thanh toán và trừ kho).
* **Admin Service DB:** Quản lý `ADMIN_ACTION_LOGS`, `CONTENT_MODERATION_LOGS`.
* **Notification Service DB:** Quản lý `USER_NOTIFICATIONS`, `DEVICE_TOKENS`.

### 2.2. NoSQL Database (MongoDB)
Được sử dụng cho dữ liệu có tính phi cấu trúc (schema-less), số lượng bản ghi lớn, cần tốc độ ghi và khả năng scale ngang linh hoạt.
* **Social Service DB:** Lưu trữ `POSTS`, `COMMENTS`, `FEEDS`. Nội dung bài post có thể chứa các array hình ảnh, text phong phú, rất phù hợp với Document Database.

### 2.3. Caching & In-Memory Store (Redis)
* **Session Management:** Lưu trữ Refresh Tokens, Blacklisted Tokens của Auth Service.
* **Cart Management:** Lưu Giỏ hàng (Cart) của Commerce Service (vì giỏ hàng thay đổi liên tục, không cần lưu DB cho đến khi checkout).
* **Distributed Locks:** Khóa tạm thời (Locking) kho hàng khi user đang ở màn hình thanh toán payOS để tránh bán lố (Overselling).

## 3. Xử lý Dữ liệu phân tán (Data Management)

### 3.1. Chống Overselling (Inventory Locking)
* Khi Buyer nhấn "Thanh toán", Commerce Service tạo `Order` và dùng Redis để tạm giữ số lượng tồn kho (Hold Stock) trong một khoảng thời gian (vd: 15 phút).
* Nếu Webhook payOS báo `PAYMENT_SUCCESS`, xác nhận trừ tồn kho trong PostgreSQL.
* Nếu hết giờ mà payOS không báo thành công, Job quét và nhả Redis Lock, cộng lại tồn kho.

### 3.2. Data Replication (Lưu bản sao dữ liệu)
Vì Social Service (MongoDB) cần hiển thị thông tin User (Tên, Avatar), nó không thể gọi Auth API mỗi lần render Feed. 
* **Giải pháp:** Social Service lắng nghe event `USER_CREATED` và `USER_UPDATED` từ Auth Service, sau đó lưu một bản sao cục bộ (denormalized data) chứa `user_id`, `display_name`, `avatar_url` vào MongoDB của chính nó.