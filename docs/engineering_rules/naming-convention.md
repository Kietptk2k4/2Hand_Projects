# Naming Convention - 2Hands Project

## 1. Source Code (Java/Kotlin Style)
- **Classes / Interfaces:** PascalCase (vd: `AuthService`, `ProductController`).
- **Variables / Methods:** camelCase (vd: `lastLoginAt`, `calculateTotalAmount()`).
- **Constants:** UPPER_SNAKE_CASE (vd: `MAX_RETRY_COUNT`).
- **Packages:** lowercase (vd: `com.twohands.auth.service`).

## 2. Database (PostgreSQL & MongoDB)
- **Tables:** snake_case, số nhiều (plural) (vd: `users`, `order_items`).
- **Columns:** snake_case (vd: `password_hash`, `created_at`).
- **Primary Key:** Luôn đặt tên là `id` (ưu tiên kiểu UUID).
- **Foreign Key:** `table_name_singular_id` (vd: `user_id`, `role_id`).

## 3. Event-Driven (Message Broker)
- **Topics / Channels:** `service.domain.action` (vd: `auth.user.created`, `commerce.order.placed`).
- **Event Types:** UPPER_SNAKE_CASE (vd: `USER_CREATED`, `PAYMENT_SUCCESS`).

## 4. API Endpoints
- **Resource path:** Danh từ, số nhiều, lowercase (vd: `/products`, `/orders`).
- **Action:** Sử dụng HTTP Methods thay vì đưa hành động vào URL.
  - Đúng: `GET /users`
  - Sai: `GET /getUsers`