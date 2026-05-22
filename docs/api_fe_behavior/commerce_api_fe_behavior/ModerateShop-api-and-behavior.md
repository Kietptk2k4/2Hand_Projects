# Moderate Shop – API & Behavior

## 1. Business Goal

Admin suspend, dong hoac khoi phuc shop. Shop khong `ACTIVE` bi an khoi discovery, chan add-cart/checkout; cart items cua seller duoc danh `INVALID_PRODUCT` khi suspend/close.

## 2. API Contract

- **Method:** POST
- **URL:** `/commerce/api/v1/admin/shops/{shopId}/moderate`
- **Auth:** Bearer JWT + permission theo `action`

### Request body

| Field | Type | Required | Mo ta |
|-------|------|----------|-------|
| `action` | string | yes | `SUSPEND`, `CLOSE`, `RESTORE` |
| `reason` | string | yes | Ly do moderation |

```json
{
  "action": "SUSPEND",
  "reason": "Vi pham chinh sach ban hang"
}
```

### Permissions (MVP)

| Action | Permission |
|--------|------------|
| `SUSPEND` | `COMMERCE_SHOP_SUSPEND` |
| `CLOSE` | `COMMERCE_SHOP_CLOSE` |
| `RESTORE` | `COMMERCE_SHOP_SUSPEND` hoac `COMMERCE_SHOP_CLOSE` |

Role `ADMIN` duoc chap nhan khi JWT chua co claim `permissions`.

## 3. Status transitions

| From | Action | To |
|------|--------|-----|
| `ACTIVE` | `SUSPEND` | `SUSPENDED` |
| `ACTIVE` | `CLOSE` | `CLOSED` |
| `SUSPENDED` | `RESTORE` | `ACTIVE` |
| `SUSPENDED` | `CLOSE` | `CLOSED` |
| `CLOSED` | `RESTORE` | `ACTIVE` |

`RESTORE` **khong** tu dong publish lai product da archive/removed.

## 4. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Suspend shop thanh cong.",
  "data": {
    "shop_id": "770e8400-e29b-41d4-a716-446655440002",
    "seller_id": "880e8400-e29b-41d4-a716-446655440003",
    "shop_name": "Cua hang ABC",
    "status": "SUSPENDED",
    "previous_status": "ACTIVE",
    "already_moderated": false,
    "cart_items_invalidated": 5,
    "moderated_at": "2026-05-21T10:00:00Z"
  },
  "errors": null,
  "timestamp": "2026-05-21T10:00:01Z"
}
```

## 5. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 401 | `COMMERCE-401` | Thieu JWT |
| 403 | `COMMERCE-403` | Thieu permission |
| 400 | `COMMERCE-400-VALIDATION` | `reason` trong |
| 400 | `COMMERCE-400-SHOP-MODERATION` | `action` khong hop le |
| 404 | `COMMERCE-404-SHOP` | Shop khong ton tai |
| 409 | `COMMERCE-409-SHOP-STATUS` | Transition khong hop le |

## 6. Database & Events

- Write: `seller_shops`, `cart_items` (optional), `outbox_events`
- Outbox:
  - `COMMERCE_SHOP_SUSPENDED` → `commerce.shop.suspended`
  - `COMMERCE_SHOP_CLOSED` → `commerce.shop.closed`
  - `COMMERCE_SHOP_RESTORED` → `commerce.shop.restored`

## 7. Related

- FR: `docs/feature_requirements/commerce/FR_ModerateShop.md`
- Flow: `docs/business_flow/commerce_business_flow/admin-moderation-flow.md` §6
