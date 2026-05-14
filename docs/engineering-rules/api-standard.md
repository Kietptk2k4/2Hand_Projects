# API Standard - 2Hands Project

## 1. Base URL
- Cấu trúc: `https://api.2hands.vn/{service-name}/api/{version}/{resource}`
- Ví dụ: `https://api.2hands.vn/auth/api/v1/users`

## 2. HTTP Methods
- **GET:** Lấy dữ liệu (không thay đổi trạng thái hệ thống).
- **POST:** Tạo mới tài nguyên hoặc thực hiện hành động nhạy cảm (Login).
- **PUT:** Cập nhật toàn bộ tài nguyên.
- **PATCH:** Cập nhật một phần tài nguyên.
- **DELETE:** Xóa tài nguyên (mặc định là Soft Delete).

## 3. Cấu trúc Response chuẩn
Mọi API phải trả về cùng một cấu trúc JSON:
```json
{
  "code": 200,          // HTTP Status Code hoặc Business Code
  "success": true,      // true/false
  "message": "Success", // Thông báo thân thiện cho FE
  "data": { ... },      // Dữ liệu trả về (null nếu là lỗi)
  "errors": null,       // Chi tiết lỗi nếu success = false
  "timestamp": "2023-10-27T10:00:00Z"
}