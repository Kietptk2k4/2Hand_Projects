# Master Specification - Hệ thống 2Hands (MVP)

## 1. Tổng quan dự án (Project Overview)
Hệ thống 2Hands là nền tảng thương mại điện tử kết hợp mạng xã hội, được thiết kế theo kiến trúc Microservices và Event-Driven Architecture (sử dụng Outbox Pattern để liên lạc bất đồng bộ). Tài liệu Master Specification này định nghĩa cái nhìn toàn cảnh về phạm vi của Minimum Viable Product (MVP), luồng giao tiếp, và giới hạn của các dịch vụ trong hệ thống.

### 1.1 Product vertical (MVP)

MVP **không** nhắm general multi-vendor đa ngành. Vertical sản phẩm hiện tại:

> **Sàn đồ thời trang second-hand (C2C closet marketplace) + social discovery**

| Khía cạnh | Quyết định MVP |
|-----------|----------------|
| Ngành hàng | Chỉ thời trang & phụ kiện (seed category, không công cụ/điện/...) |
| Listing | 1 sản phẩm ≈ 1 món unique (`stock_quantity` 0 hoặc 1) |
| Tình trạng | `LIKE_NEW`, `GOOD`, `FAIR`, `USED` (không `NEW` mặc định) |
| Catalog | Cây category thời trang + bảng `brands` (migration V2/V3) |
| Discovery | Catalog Commerce + feed Social (OOTD, tag sản phẩm) |

**Tài liệu vertical (đọc trước khi implement catalog/FE mock):**

- `docs/product-vision/fashion-secondhand-vertical.md` — quyết định business
- `docs/database/commerce-catalog-seed.md` — UUID category/brand cụ thể

Kiến trúc 5 service giữ generic; vertical được cấu hình bằng **data + quy ước nghiệp vụ**, không đổi service boundary.

## 2. Kiến trúc Hệ thống (System Architecture)
Hệ thống bao gồm 5 Microservices cốt lõi:
* **Auth Service:** Quản lý người dùng, phân quyền (Role/Permission), xác thực (Email/OAuth) và quản lý phiên làm việc.
* **Social Service:** Cung cấp tính năng mạng xã hội như đăng bài, bình luận (lưu trữ trên MongoDB), thả tim, lưu bài và theo dõi (PostgreSQL).
* **Commerce Service:** Xử lý toàn bộ luồng mua bán, giỏ hàng, thanh toán (PayOS/COD), giao hàng (GHN), và đánh giá sản phẩm.
* **Admin Service:** Dành cho ban quản trị để kiểm duyệt nội dung (Sản phẩm, Bài viết, Người dùng, Shop) và điều chỉnh cấu hình hệ thống.
* **Notification Service:** Trạm trung chuyển nhận sự kiện và phát thông báo In-app, Push (FCM), và Email.

## 3. Định nghĩa Đối tượng Người dùng (Actor Definitions)

| Actor | Mô tả & Vai trò |
| :--- | :--- |
| **Buyer (Người mua)** | Người dùng tiêu chuẩn. Tương tác với mạng xã hội, thêm hàng vào giỏ, đặt hàng, thanh toán và để lại đánh giá. |
| **Seller (Người bán)** | Chủ closet / shop nhỏ (1 shop/user). Đăng từng món second-hand (ảnh, size, condition), stock thường = 1, xử lý đơn & shipment. |
| **Admin/Moderator** | Nhân sự quản trị có Role và Permission từ Auth Service. Kiểm duyệt tài khoản, sản phẩm, và thiết lập cấu hình. |
| **System** | Background jobs xử lý retry events, expire payment, dọn dẹp invalid cart items và tính toán stock. |

## 4. Phạm vi Tính năng Cốt lõi (MVP Scope)

| Service | Danh sách tính năng chính (MVP) |
| :--- | :--- |
| **Auth** | Register/Login, OAuth, Refresh Tokens, Quản lý Profile, Soft Delete, Phát Event User. |
| **Social** | Global/Following Feed, Post/Comment (MongoDB), Like, Save, Follow, Tag sản phẩm. |
| **Commerce** | Shop/listing thời trang second-hand, category + brand catalog, Order, Checkout, Lock tồn kho, PayOS/COD, GHN, Review. Chi tiết vertical: `docs/product-vision/fashion-secondhand-vertical.md`. |
| **Admin** | Phân quyền Admin, Kiểm duyệt (Suspend User, Remove Product, Hide Review), Audit Log. |
| **Notification** | In-app, Push (FCM), Email cơ bản, Quản lý Notification Settings theo người dùng. |

## 5. Các Luồng Tích hợp Liên dịch vụ (Key Cross-Service Integrations)
* **Xử lý Vi phạm (Enforcement):** Admin bắn event `USER_SUSPENDED` → Auth cập nhật trạng thái, revoke token → Social chặn đăng bài → Notification gửi thông báo.
* **Checkout & Tồn kho:** Khi Buyer đặt hàng → Reserved Stock tăng. Khi `PAYMENT_SUCCESS` → Available Stock giảm thực tế. Nếu thất bại → Revert Stock.
* **Payment & Shipment Lifecycle:** Đơn hàng tạo ra chờ thanh toán. Thanh toán xong chờ GHN giao hàng. Webhook từ GHN sẽ tự động đẩy trạng thái Shipment và kéo theo cập nhật trạng thái Order.
