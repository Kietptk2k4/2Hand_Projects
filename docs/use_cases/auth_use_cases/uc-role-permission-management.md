# UC - Role & Permission Management

## 1. Overview
Cung cấp khả năng phân quyền RBAC (Role-Based Access Control). Giúp Admin cấp quyền điều hành cho các user, và giúp API Gateway kiểm tra quyền truy cập.

## 2. Actors
* **Admin:** Người có quyền quản trị cao nhất.
* **System (Interceptor):** Gateway hoặc Middleware kiểm tra quyền.

## 3. Sub-Use Cases

### 3.1. Gán / Thu hồi Role cho User (Assign/Revoke Role)
* **Pre-conditions:** Người thực hiện có quyền `ASSIGN_ROLE`. User mục tiêu tồn tại.
* **Main Flow:**
  1. Admin gửi yêu cầu gán/thu hồi `role_id` cho `user_id`.
  2. Cập nhật bảng `USER_ROLES` (Insert / Delete).
  3. (Tùy chọn) Invalidate session của User đó để họ phải đăng nhập lại và lấy JWT chứa Roles/Permissions mới.

### 3.2. Xem danh sách Role và Permission
* **Main Flow:**
  1. Admin xem danh sách hệ thống.
  2. Query từ `ROLES`, `PERMISSIONS` và `ROLE_PERMISSIONS`.

### 3.3. Authorize Request theo Permission (Check Quyền)
* **Pre-conditions:** Client gọi API có gửi kèm JWT Access Token.
* **Main Flow:**
  1. System (Interceptor/Gateway) parse JWT để lấy mảng Permissions.
  2. So khớp mảng Permission đó với Permission yêu cầu của API (ví dụ `@PreAuthorize("hasAuthority('DELETE_POST')")`).
  3. Nếu có quyền -> Cho phép đi tiếp (Allow). Không có -> Trả về 403 Forbidden (Deny).