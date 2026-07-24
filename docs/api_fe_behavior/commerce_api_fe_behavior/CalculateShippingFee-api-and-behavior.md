# Calculate Shipping Fee – API & Behavior

## 1. Business Goal

Cho phep buyer uoc tinh phi van chuyen theo seller group truoc checkout, dua tren dia chi nhan hang, dia chi lay hang cua seller, trong luong san pham va loai van chuyen. Quote **khong** tao order/shipment.

## 2. API Contract

- **Method:** POST
- **URL:** `/commerce/api/v1/shipping/fee`
- **Auth:** Bearer JWT (required)

### Request body

| Field            | Type          | Required | Mo ta                                      |
|------------------|---------------|----------|--------------------------------------------|
| `cart_item_ids`  | UUID[]        | yes      | Cart items can tinh phi                    |
| `address_id`     | UUID          | yes      | Dia chi giao hang cua buyer                |
| `shipment_type`  | string (enum) | no       | `STANDARD` (mac dinh), `EXPRESS`, `SAME_DAY` |

```json
{
  "cart_item_ids": ["770e8400-e29b-41d4-a716-446655440002"],
  "address_id": "880e8400-e29b-41d4-a716-446655440003",
  "shipment_type": "EXPRESS"
}
```

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Tinh phi van chuyen thanh cong.",
  "data": {
    "seller_groups": [
      {
        "seller_id": "990e8400-e29b-41d4-a716-446655440004",
        "shop_id": "aa0e8400-e29b-41d4-a716-446655440005",
        "shipping_fee": 52500,
        "shipping_fee_origin": 52500,
        "estimated_delivery_date": "2026-05-22",
        "shipment_type": "EXPRESS"
      }
    ],
    "total_shipping_fee": 52500
  },
  "errors": null,
  "timestamp": "2026-05-21T10:30:00.123Z"
}
```

## 4. Response – Error

| HTTP | Code string                     | Mo ta                                           |
|------|---------------------------------|-------------------------------------------------|
| 400  | `COMMERCE-400-VALIDATION`       | Thieu/invalid `cart_item_ids`, `address_id`     |
| 401  | `COMMERCE-401`                  | Khong co JWT                                    |
| 404  | `COMMERCE-404-CART-ITEM`        | Cart item khong thuoc cart buyer                |
| 404  | `COMMERCE-404-ADDRESS`          | Dia chi khong ton tai/khong thuoc buyer         |
| 404  | `COMMERCE-404-PRODUCT`          | Product lien ket cart item khong ton tai        |
| 409  | `COMMERCE-409-SHIPPING-PROFILE` | Seller chua co shipping profile               |
| 503  | `COMMERCE-503-SHIPPING`         | GHN bat, khong mock fallback, API chua san sang |
| 500  | `COMMERCE-500`                  | Loi server                                      |

## 5. Business Rules

- Chi tinh cho cart items va address thuoc buyer (JWT `user_id`).
- Group items theo **seller**; `total_weight_gram = sum(product.weight_gram * quantity)`.
- Seller phai co `seller_shipping_profiles` de tinh phi.
- `total_shipping_fee = sum(shipping_fee tung seller group)`.
- MVP mock: `shipping_fee_origin` = `shipping_fee` (chua co giam gia GHN).
- `estimated_delivery_date`:
  - Mock / GHN disabled / mock fallback: heuristic `STANDARD` +3 ngay, `EXPRESS` +1 ngay, `SAME_DAY` cung ngay.
  - GHN live: goi **leadtime** song song voi Calculate Fee; map `leadtime` (unix) → `LocalDate`.
  - Neu leadtime fail/empty: van tra fee GHN, ETA fallback heuristic (WARN log).
- GHN disabled hoac `COMMERCE_GHN_MOCK_FALLBACK_ENABLED=true` → dung mock calculator.
- Checkout/order total se tinh lai phi ship; quote khong dam bao cuoi cung.

## 6. Edge Cases

- Multi-seller cart → nhieu phan tu trong `seller_groups`.
- `shipment_type` null → `STANDARD`.
- Trung `cart_item_id` trong request → xu ly theo danh sach distinct.

## 7. Data Dependencies

- Read-only: `carts`, `cart_items`, `products`, `user_addresses`, `seller_shipping_profiles`, `seller_shops`.

## 8. FE Integration Notes

- Co the goi doc lap truoc checkout preview hoac cung luong voi `POST /commerce/api/v1/checkout/quote`.
- Hien `estimated_delivery_date` (FE checkout: "Du kien giao"); khi GHN live uu tien leadtime, khi fail van co heuristic.
- Khong luu quote vao DB.
