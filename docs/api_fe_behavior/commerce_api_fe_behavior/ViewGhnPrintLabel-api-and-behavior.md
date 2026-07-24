# View GHN Print Label – API & Behavior

## 1. Business Goal

Cho seller tao link in van don GHN (phieu gui hang) tu shipment da co `ghn_order_code`, mo tren trinh duyet de in.

## 2. API Contract

- **Method:** GET
- **URL:** `/commerce/api/v1/seller/shipments/{shipmentId}/ghn/print-label`
- **Auth:** Bearer JWT (seller ownership)

### Query params

| Field | Type | Required | Mo ta |
|-------|------|----------|-------|
| `format` | string | no | `a5` (default), `80x80`, `52x70` |

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Tao link in van don GHN thanh cong.",
  "data": {
    "shipment_id": "770e8400-e29b-41d4-a716-446655440002",
    "ghn_order_code": "5ENLKKHD",
    "format": "a5",
    "print_token": "e27db030-a1bf-11ea-b421-6a186c15e40e",
    "print_url": "https://dev-online-gateway.ghn.vn/a5/public-api/printA5?token=e27db030-a1bf-11ea-b421-6a186c15e40e",
    "expires_in_minutes": 30
  },
  "errors": null,
  "timestamp": "2026-05-21T12:00:01Z"
}
```

## 4. Response – Error

| HTTP | Code | Mo ta |
|------|------|-------|
| 401 | `COMMERCE-401` | Thieu JWT |
| 400 | `COMMERCE-400-VALIDATION` | `format` khong hop le |
| 400 | `COMMERCE-400` | Shipment chua co `ghn_order_code` |
| 400 | `COMMERCE-400-SHIPMENT-CARRIER` | Carrier khong phai GHN |
| 404 | `COMMERCE-404-SHIPMENT` | Khong ton tai / khong thuoc seller |
| 503 | `COMMERCE-503-GHN` | GHN gen-token that bai |

## 5. Business Rules

- Chi shipment `carrier=GHN` va co `ghn_order_code`.
- BE goi GHN `POST /v2/a5/gen-token` roi ghep URL print theo format.
- Token GHN het han ~30 phut (`expires_in_minutes`).

## 6. FE Integration Notes

- Seller shipment detail: section **In van don GHN** khi `canPrintGhnLabel`.
- Default format `a5`; cho chon `80x80` / `52x70`.
- Thanh cong: `window.open(print_url)`; neu popup bi chan, hien toast kem URL.
- Khong can luu `print_token` dai han — moi lan in goi lai API.

## 7. Related

- Doc GHN: `docs/ghn/GHN.Print Order.txt`
- Manage seller: [ManageSellerShipment-api-and-behavior.md](./ManageSellerShipment-api-and-behavior.md)
