# GHN Webhook Demo (giả lập callback qua ngrok)

## Mục tiêu

Khi chưa đăng ký webhook với GHN, vẫn demo cập nhật status shipment **tự động** bằng cách POST payload giống callback GHN vào endpoint thật:

`POST /commerce/api/v1/shipments/webhooks/ghn`

Máy khác (hoặc cùng máy) gọi URL ngrok công khai → Commerce cập nhật DB → F5 trang tracking trên app 2Hands.

## Điều kiện

1. `commerce-service` đang chạy.
2. Ngrok expose commerce (HTTPS), ví dụ profile `payos` / `ngrok-commerce`.
3. Có shipment GHN với `ghn_order_code` (sau khi seller tạo vận đơn).
4. Nếu `COMMERCE_GHN_WEBHOOK_SECRET` có giá trị → demo phải gửi cùng secret (header `Token`).

## Checklist demo với thầy

1. Seller tạo shipment GHN trên 2Hands → copy **mã GHN** (`ghn_order_code`).
2. Lấy URL ngrok:

```powershell
cd Infrastructure\scripts
.\print-ghn-webhook-url.ps1
```

hoặc:

```sh
./print-ghn-webhook-url.sh
```

3. Máy B (hoặc máy A): mở file

`Infrastructure/scripts/ghn-webhook-demo.html`

4. Dán:
   - Base URL: `https://xxxx.ngrok-free.dev` (không cần path)
   - Secret: giá trị `COMMERCE_GHN_WEBHOOK_SECRET` (nếu có)
   - OrderCode: `ghn_order_code`
5. Bấm status theo thứ tự hợp lệ, ví dụ:
   - `delivering` (→ SHIPPED)
   - rồi `delivered` (→ DELIVERED)
6. Trên app 2Hands: mở / F5 trang tracking  
   `/commerce/orders/{orderId}/shipments/{shipmentId}`  
   → status + timeline đã cập nhật.

## CLI (PowerShell)

```powershell
cd Infrastructure\scripts

# Tự lấy ngrok URL từ http://127.0.0.1:4040
.\demo-ghn-webhook.ps1 -OrderCode "5ENLKKHD" -Status delivering -Secret "your-secret"

.\demo-ghn-webhook.ps1 -OrderCode "5ENLKKHD" -Status delivered -Secret "your-secret"

# Hoặc chỉ định URL tường minh (máy remote / tunnel khác)
.\demo-ghn-webhook.ps1 `
  -WebhookBaseUrl "https://xxxx.ngrok-free.dev" `
  -OrderCode "5ENLKKHD" `
  -Status delivered `
  -Secret "your-secret"
```

Status hợp lệ: `ready_to_pick`, `picking`, `delivering`, `delivered`, `cancel`, `return`.

## Transition policy (Commerce)

Không nhảy lung tung. Ví dụ shipment `PENDING`:

| Raw Status     | Domain status  | Ghi chú        |
|----------------|----------------|----------------|
| ready_to_pick  | READY_TO_SHIP  | OK từ PENDING  |
| picking        | PICKING_UP     | OK từ PENDING  |
| delivering     | SHIPPED        | OK từ PENDING  |
| delivered      | DELIVERED      | Chỉ từ SHIPPED |
| cancel         | CANCELLED      | Trước khi ship |

Khuyến nghị demo happy path: `delivering` → `delivered`.

## Files

| File | Mục đích |
|------|----------|
| `Infrastructure/scripts/ghn-webhook-demo.html` | UI bấm status (máy bất kỳ) |
| `Infrastructure/scripts/demo-ghn-webhook.ps1` | CLI POST webhook |
| `Infrastructure/scripts/print-ghn-webhook-url.ps1` | In URL ngrok + hướng dẫn |
| `Infrastructure/scripts/print-ghn-webhook-url.sh` | Bản shell |

## Lưu ý

- Đây là tool **dev/demo**, không phải đăng ký webhook GHN production.
- Ngrok free đổi domain mỗi lần restart → máy B phải cập nhật base URL.
- Header `ngrok-skip-browser-warning: true` đã được tool gửi sẵn.
