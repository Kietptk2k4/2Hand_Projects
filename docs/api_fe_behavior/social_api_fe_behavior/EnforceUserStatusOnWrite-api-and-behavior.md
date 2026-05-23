# Enforce User Status On Write – API & Behavior

## 1. Business Goal

Mọi thao tác **ghi** trên Social (post, comment, like, save, follow, ghi search history) phải kiểm tra trạng thái user trong `user_projections` (đồng bộ từ Auth/Admin qua Kafka). User `SUSPENDED` / `DELETED` hoặc chưa có projection không được ghi.

## 2. Phạm vi

| Áp dụng | Không áp dụng |
|---------|----------------|
| Create/Edit/Delete post, Like/Unlike, Save/Unsave | View feed, view post, list comments |
| Comment/Reply/Delete comment, Like comment | View profile, view saved posts (read) |
| Follow/Unfollow | Search posts (kết quả vẫn trả về; chỉ **không** lưu history nếu bị chặn) |
| Ghi `search_history` sau search | |

## 3. Error contract (write bị chặn)

| Tình huống | HTTP | `code` | Message mẫu |
|------------|------|--------|-------------|
| `SUSPENDED` | 403 | `SOCIAL-403-SUSPENDED` | Tai khoan bi dinh chi, khong the thuc hien hanh dong nay. |
| `DELETED` hoặc thiếu projection | 403 | `SOCIAL-403` | Access denied |
| Chưa đăng nhập | 401 | `SOCIAL-401` | Authentication required |

Envelope chuẩn: `success: false`, `errors` theo API standard.

## 4. Implementation (FE không gọi API riêng)

- Guard: `UserWriteGuard.assertCanWrite(actorUserId)` ở application layer (đầu mỗi write use case).
- `actorUserId` lấy từ JWT, không từ body.
- Moderator/Admin role (`MODERATOR`, `ADMIN`) **bypass** guard khi xóa post/comment qua API moderation (JWT hệ thống).
- Read API không gọi guard; user suspended vẫn có thể xem feed/post nếu product cho phép.

## 5. FE handling

- **403 + `SOCIAL-403-SUSPENDED`:** Hiển thị toast/dialog một lần; disable nút tạo/sửa/like/comment/follow; vẫn cho phép điều hướng read-only.
- **403 + `SOCIAL-403`:** Coi như không có quyền ghi (có thể thiếu projection — retry sau vài giây nếu user mới đăng ký).
- Không hiển thị chi tiết enforcement từ server.

## 6. Related

- `FR_ConsumeAuthUserEvents` — cập nhật `user_projections.status`
- `auth/FR_ApplyUserEnforcement` — nguồn suspend phía Auth
