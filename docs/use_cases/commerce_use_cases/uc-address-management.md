# UC - Address Management

## 1. Overview

Use case nay mo ta nghiep vu buyer quan ly dia chi giao hang trong Commerce Service. `user_addresses` la address book mutable cua buyer. Khi order/shipment duoc tao, Commerce Service phai tao `shipping_address_snapshots` de giu lich su dia chi giao hang khong bi thay doi khi buyer sua/xoa address sau nay.

## 2. Actors

- **Buyer:** Them, sua, xoa, chon dia chi mac dinh, xem danh sach dia chi.
- **System:** Tao shipping address snapshot khi checkout/shipment.

## 3. Related Data

- `user_addresses`
- `shipping_address_snapshots`
- `shipments`
- `orders`

## 4. Business Rules

- Buyer chi quan ly dia chi cua minh.
- `user_id` lay tu JWT, khong lay tu body.
- Moi buyer co the co nhieu address.
- Moi buyer chi co toi da mot default address.
- Address book la mutable; shipment phai dung snapshot immutable.
- Cap nhat/xoa address sau checkout khong lam thay doi shipment da tao.
- First address cua buyer nen tu dong la default.

## 5. Sub-Use Cases

### 5.1. Create Address

**Goal:** Buyer them dia chi giao hang moi.

**Preconditions:**

- Buyer da dang nhap.

**Main Flow:**

1. Buyer gui thong tin dia chi.
2. System validate `receiver_name`, `phone`, `province_code`, `district_code`, `ward_code`, `address_detail`.
3. System tao `user_addresses` theo `user_id` tu JWT.
4. Neu day la address dau tien, set `is_default = true`.
5. Neu request set default, system unset default address cu trong cung transaction.
6. System tra address vua tao.

**Exception Flow:**

- Payload thieu field -> 400.
- Phone invalid -> 400.
- Concurrent default conflict -> retry/unset old default trong transaction.

**Postconditions:**

- Address moi duoc luu.
- Buyer van co toi da mot default address.

### 5.2. Update Address

**Goal:** Buyer cap nhat dia chi da luu.

**Preconditions:**

- Buyer da dang nhap.
- Address ton tai va thuoc buyer.

**Main Flow:**

1. Buyer gui request update address.
2. System load address by `id` va `user_id`.
3. System validate payload.
4. System update fields.
5. Neu request set default, system unset default cu va set address nay default.
6. System tra address moi.

**Exception Flow:**

- Address not found/not owned -> 404.
- Payload invalid -> 400.

**Postconditions:**

- Address book duoc cap nhat.
- Existing shipping snapshots khong thay doi.

### 5.3. Delete Address

**Goal:** Buyer xoa dia chi khoi address book.

**Preconditions:**

- Buyer da dang nhap.
- Address thuoc buyer.

**Main Flow:**

1. Buyer request delete address.
2. System load address by `id` va `user_id`.
3. System delete address.
4. Neu address bi xoa la default va buyer con address khac, system co the set address khac lam default.
5. System tra success.

**Exception Flow:**

- Address not found -> 404.

**Postconditions:**

- Address khong con trong address book.
- Shipment history van giu dia chi qua snapshot.

### 5.4. Set Default Address

**Goal:** Buyer chon dia chi mac dinh cho checkout.

**Preconditions:**

- Buyer da dang nhap.
- Address thuoc buyer.

**Main Flow:**

1. Buyer chon address id.
2. System load address by `id` va `user_id`.
3. System unset all default addresses cua buyer.
4. System set target address `is_default = true`.
5. System tra danh sach/address moi.

**Exception Flow:**

- Address not found -> 404.
- Unique default conflict -> retry transaction.

**Postconditions:**

- Buyer co dung mot default address neu co address.

### 5.5. View Address List

**Goal:** Buyer xem danh sach dia chi cua minh.

**Preconditions:**

- Buyer da dang nhap.

**Main Flow:**

1. Buyer request address list.
2. System query `user_addresses` by `user_id`.
3. System sort default first, then recent updated/created.
4. System tra danh sach.

**Exception Flow:**

- Khong co address -> tra empty list.

**Postconditions:**

- Khong thay doi database.

### 5.6. Create Shipping Address Snapshot

**Goal:** System dong bang dia chi giao hang tai thoi diem tao shipment/order.

**Preconditions:**

- Buyer chon address hop le.
- Shipment duoc tao hoac dang duoc tao.

**Main Flow:**

1. System load selected address by `address_id` va `buyer_id`.
2. System tao shipment.
3. System copy address fields vao `shipping_address_snapshots`.
4. System gan snapshot voi `shipment_id`.

**Exception Flow:**

- Address not found/not owned -> checkout/shipment creation fail.
- Snapshot duplicate for shipment -> 409/data conflict.

**Postconditions:**

- Shipment co one immutable shipping address snapshot.

## 6. Validation Rules

Required fields:

- `receiver_name`
- `phone`
- `province_code`
- `district_code`
- `ward_code`
- `address_detail`

Recommended:

- Generate `full_address` from location labels/codes and detail when creating snapshot.
- Use partial unique index `UNIQUE(user_id) WHERE is_default = true`.

## 7. Security

- JWT required for all address APIs.
- Buyer chi truy cap address cua minh.
- Checkout chi duoc dung address thuoc buyer.

## 8. Acceptance Criteria

- Buyer can CRUD only own addresses.
- First address becomes default.
- Set default is atomic and leaves only one default.
- Deleting/updating address does not change shipping snapshots.
- Checkout creates immutable shipping snapshot from selected address.

