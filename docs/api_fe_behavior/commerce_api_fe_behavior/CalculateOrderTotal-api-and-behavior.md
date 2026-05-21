# Calculate Order Total – API & Behavior

## 1. Business Goal

Cho phep buyer xem truoc tong tien don hang (subtotal + phi ship + final amount) truoc khi checkout. Quote **khong** reserve ton kho; checkout se tinh lai va validate.

## 2. API Contract

- **Method:** POST
- **URL:** `/commerce/api/v1/checkout/quote`
- **Auth:** Bearer JWT (required)

### Request body

| Field            | Type           | Required | Mo ta                                      |
|------------------|----------------|----------|--------------------------------------------|
| `cart_item_ids`  | UUID[]         | yes      | Cac dong cart duoc chon de quote           |
| `address_id`     | UUID           | yes      | Dia chi giao hang cua buyer                |
| `shipment_type`  | string (enum)  | no       | `STANDARD` (mac dinh), `EXPRESS`, `SAME_DAY` |

```json
{
  "cart_item_ids": [
    "770e8400-e29b-41d4-a716-446655440002"
  ],
  "address_id": "880e8400-e29b-41d4-a716-446655440003",
  "shipment_type": "STANDARD"
}
```

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Tinh tong tien don hang thanh cong.",
  "data": {
    "items": [
      {
        "cart_item_id": "770e8400-e29b-41d4-a716-446655440002",
        "unit_price": 900000,
        "quantity": 2,
        "item_total": 1800000,
        "shipping_fee_allocated": 40000
      }
    ],
    "total_amount": 1800000,
    "shipping_fee": 40000,
    "final_amount": 1840000,
    "seller_shipping_groups": [
      {
        "seller_id": "990e8400-e29b-41d4-a716-446655440004",
        "shop_id": "aa0e8400-e29b-41d4-a716-446655440005",
        "shipping_fee": 40000,
        "shipment_type": "STANDARD"
      }
    ]
  },
  "errors": null,
  "timestamp": "2026-05-21T10:30:00.123Z"
}
```

## 4. Response – Error

| HTTP | Code string                         | Mo ta                                                |
|------|-------------------------------------|------------------------------------------------------|
| 400  | `COMMERCE-400-VALIDATION`           | Thieu/invalid `cart_item_ids`, `address_id`          |
| 401  | `COMMERCE-401`                      | Khong co JWT                                         |
| 404  | `COMMERCE-404-CART-ITEM`            | Cart item khong thuoc cart cua buyer                 |
| 404  | `COMMERCE-404-ADDRESS`              | Dia chi khong ton tai hoac khong thuoc buyer         |
| 404  | `COMMERCE-404-PRODUCT`              | Product lien ket cart item khong ton tai           |
| 409  | `COMMERCE-409-PRICE`                | Khong co active price                                |
| 409  | `COMMERCE-409-SHIPPING-PROFILE`     | Seller chua co shipping profile                    |
| 503  | `COMMERCE-503-SHIPPING`             | GHN bat nhung khong co mock fallback                 |
| 500  | `COMMERCE-500`                      | Loi server                                           |

## 5. Business Rules

- `unit_price` = `sale_price` neu hop le, nguoc lai `price` (active price hien tai).
- `item_total = unit_price * quantity`.
- `total_amount = sum(item_total)`.
- Phi ship tinh **theo seller group** (moi seller mot nhom trong MVP).
- `shipping_fee = sum(phi ship tung seller group)`.
- `shipping_fee_allocated` chia theo ty le `item_total` trong cung seller group; tong allocated = phi ship group.
- `final_amount = total_amount + shipping_fee`.
- MVP: neu `COMMERCE_GHN_ENABLED=false` dung mock calculator; neu GHN bat va `COMMERCE_GHN_MOCK_FALLBACK_ENABLED=true` van dung mock (GHN API chua tich hop).
- Quote khong dam bao gia cuoi cung; `FR_CheckoutFromCart` se tinh lai.

## 6. Edge Cases

- Buyer chua co cart → 404 cart item.
- Trung `cart_item_id` trong request → load theo danh sach distinct.
- Multi-seller: moi seller mot entry trong `seller_shipping_groups`.
- `shipment_type` null → `STANDARD`.

## 7. Data Dependencies

- Read-only: `carts`, `cart_items`, `products`, `product_prices`, `seller_shops`, `seller_shipping_profiles`, `user_addresses`.
- Khong ghi DB, khong reserve inventory.

## 8. FE Integration Notes

- Goi truoc man checkout preview; goi lai khi doi dia chi, shipment type hoac danh sach cart items.
- Hien `final_amount` cho buyer; canh bao quote co the thay doi luc checkout.
- Khong dung quote de tao payment — chi checkout API moi tao order/payment.
