# UC - Session Management

## 1. Overview
Cho phép người dùng quản lý các thiết bị đang đăng nhập của mình, tăng cường bảo mật.

## 2. Actors
* **User:** Chủ tài khoản.

## 3. Sub-Use Cases

### 3.1. Xem danh sách phiên đăng nhập (View Sessions)
* **Pre-conditions:** User đang đăng nhập.
* **Main Flow:**
  1. User yêu cầu xem thiết bị đăng nhập.
  2. Hệ thống query bảng `REFRESH_TOKEN_SESSION` với `user_id` và `status` = `ACTIVE`.
  3. Trả về danh sách chứa `device_id`, `ip_address`, `user_agent`, `created_at`.

### 3.2. Đăng xuất phiên hiện tại (Logout Current)
*(Đã mô tả trong uc-user-authentication.md, mục 3.5)*

### 3.3. Đăng xuất tất cả các phiên / Revoke Session cụ thể
* **Pre-conditions:** User đang đăng nhập.
* **Main Flow:**
  1. User chọn "Đăng xuất khỏi thiết bị này" hoặc "Đăng xuất tất cả".
  2. Hệ thống update trạng thái trong `REFRESH_TOKEN_SESSION` thành `REVOKED`.
* **Exception Flow:** User cố gắng revoke session của user khác -> Check quyền sở hữu (Owner check) -> Báo lỗi 403.

### 3.4. Theo dõi lịch sử đăng nhập (View Login History)
* **Main Flow:**
  1. User yêu cầu xem lịch sử.
  2. Hệ thống query `LOGIN_LOGS` theo `user_id` (sắp xếp giảm dần theo thời gian).
  3. Trả về lịch sử gồm cả đăng nhập thành công và thất bại.