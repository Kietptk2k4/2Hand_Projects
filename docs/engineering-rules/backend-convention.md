# Backend Development Convention - Dự án 2Hands

Tài liệu này quy định các tiêu chuẩn lập trình và cấu trúc dự án dựa trên kiến trúc Microservices của hệ thống 2Hands. Mọi thành viên phát triển Backend cần tuân thủ nghiêm ngặt các quy tắc dưới đây để đảm bảo tính đồng nhất, sạch sẽ và dễ bảo trì cho toàn bộ codebase.

## 1. Cấu trúc Package (Project Structure)

Dựa trên cấu trúc thực tế của dự án, mỗi Microservice phải được tổ chức theo các package chức năng như sau:

| Package | Trách nhiệm |
| :--- | :--- |
| **`common`** | Chứa các logic, helper dùng chung trên toàn service (ví dụ: StringUtils, DateUtils chung). |
| **`configs`** | Các lớp cấu hình hệ thống: Security, CORS, Swagger/OpenAPI, Database, Redis... |
| **`constants`** | Chứa các hằng số tĩnh, mã lỗi nội bộ, hoặc các chuỗi cố định không thay đổi. |
| **`controllers`** | Tầng tiếp nhận request. Chỉnh định tuyến URL, validate format và gọi Service. |
| **`dtos`** | Chứa các đối tượng vận chuyển dữ liệu (Request/Response). Tuyệt đối không để logic tại đây. |
| **`entities`** | Các class map 1-1 với các bảng trong Database (JPA/Hibernate hoặc MongoDB). |
| **`enums`** | Định nghĩa các tập giá trị cố định (ví dụ: UserStatus, OrderStatus, PaymentMethod). |
| **`exceptions`** | Chứa các Custom Exception và `GlobalExceptionHandler` để xử lý lỗi tập trung. |
| **`mappers`** | Chuyển đổi dữ liệu qua lại giữa Entity và DTO (Khuyến khích dùng MapStruct). |
| **`repositories`** | Tầng truy cập dữ liệu (Spring Data JPA / MongoDB Repository). |
| **`services`** | Chứa các Interface định nghĩa nghiệp vụ. |
| **`services.impl`** | Nơi triển khai (Implementation) chi tiết các logic nghiệp vụ của Interface. |
| **`utils`** | Các công cụ tiện ích nhỏ phục vụ cho các logic cục bộ trong service. |

## 2. Quy tắc Giao tiếp giữa các Tầng (Layer Communication)

Để tránh tình trạng "Spaghetti code", luồng dữ liệu phải đi theo một chiều:
1.  **Client** gọi đến **Controller**.
2.  **Controller** CHỈ được phép gọi **Service**. Không được gọi trực tiếp Repository.
3.  **Service** xử lý nghiệp vụ, gọi **Repository** để truy xuất dữ liệu.
4.  **Service** trả kết quả về **Controller** dưới dạng **DTO**.
5.  **Controller** trả DTO về cho **Client**.

**Lưu ý cực kỳ quan trọng:** Không bao giờ trả đối tượng `Entity` trực tiếp ra ngoài API. Phải luôn qua bước mapping sang `DTO`.

## 3. Quản lý Transaction & Outbox Pattern

Vì hệ thống sử dụng kiến trúc Event-Driven qua Outbox Pattern:
* Các phương thức trong `service.impl` có thao tác ghi dữ liệu (Save/Update/Delete) kèm theo phát Event bắt buộc phải có annotation `@Transactional`.
* **Thứ tự thực hiện:** `Mở Transaction -> Thực thi logic -> Ghi Entity chính -> Ghi record vào bảng outbox_events -> Commit`.
* Việc đẩy event lên Message Broker (RabbitMQ/Kafka) sẽ do một Worker riêng biệt đảm nhận (đọc từ bảng `outbox_events`).

## 4. Xử lý Lỗi (Exception Handling)

* Sử dụng **Custom Exceptions** (ví dụ: `UserNotFoundException`, `InsufficientStockException`) thay vì các exception chung chung của Java.
* Tất cả lỗi phải được bắt tại `GlobalExceptionHandler` và trả về cấu trúc Response chuẩn:
    ```json
    {
      "success": false,
      "code": "ERROR_AUTH_001",
      "message": "Mô tả lỗi thân thiện",
      "timestamp": "2023-10-27T..."
    }
    ```

## 5. Quy tắc Đặt tên (Naming Conventions)

* **Class/Interface:** PascalCase (ví dụ: `AuthService`, `OrderController`).
* **Method/Variable:** camelCase (ví dụ: `lastLoginAt`, `processOrder()`).
* **Repository:** Phải kết thúc bằng hậu tố `Repository` (ví dụ: `UserRepository`).
* **Service Interface:** Tên trực tiếp (ví dụ: `ProductService`).
* **Service Impl:** Tên Interface + `Impl` (ví dụ: `ProductServiceImpl`).
* **DTO:** Kết thúc bằng `Request` hoặc `Response` (ví dụ: `CreateUserRequest`, `LoginResponse`).

## 6. Bảo mật (Security)

* Không lưu mật khẩu dạng bản rõ (plaintext). Luôn sử dụng Bcrypt để hash.
* Mọi API (trừ Login/Register) phải được bảo vệ bởi JWT. 
* Luôn kiểm tra quyền sở hữu tài nguyên (Owner check): Ví dụ: User A không được phép cập nhật địa chỉ của User B dù biết `address_id`.

## 7. Logging

* Sử dụng `SLF4J` với log level phù hợp (`INFO` cho luồng chính, `ERROR` cho lỗi, `DEBUG` khi cần dev).
* Tuyệt đối không log các thông tin nhạy cảm như Mật khẩu, Token, OTP trong file log.