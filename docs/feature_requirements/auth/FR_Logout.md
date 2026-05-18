# Functional Requirement (FR) - Dang xuat bang Email Session (Logout)

## 1. Feature Overview
Chuc nang cho phep user ket thuc phien dang nhap hien tai mot cach an toan bang cach vo hieu hoa refresh token session tren server.

Muc tieu:
- Ngan viec tiep tuc su dung refresh token sau khi user da logout.
- Dam bao hanh vi idempotent: goi logout lap lai van tra ket qua thanh cong.

## 2. Actors
- **User:** Nguoi da dang nhap, muon dang xuat khoi thiet bi hien tai.
- **Client/App:** Web/Mobile gui refresh token de yeu cau logout.

## 3. Scope
- **In Scope:**
  - Nhan refresh token logout request.
  - Tim va vo hieu hoa session trong `REFRESH_TOKEN_SESSION`.
  - Tra ve response thanh cong ngay ca khi session da bi vo hieu hoa truoc do.
- **Out of Scope:**
  - Logout all sessions (chuc nang rieng).
  - Revoke khan cap boi admin (chuc nang rieng).
  - Login/Refresh endpoint.

## 4. Preconditions
- User da co phien dang nhap va duoc cap refresh token.
- Refresh token duoc client gui kem request logout.

## 5. Business Rules
- Session logout thanh cong khi refresh session duoc chuyen trang thai sang `LOGGED_OUT` (hoac trang thai vo hieu hoa tuong duong theo implementation).
- Neu token khong ton tai/da logout/revoked truoc do, server van tra `200` de giu idempotency.
- Logout chi vo hieu hoa phien hien tai, khong tac dong toan bo phien khac.
- Client phai tu xoa token local sau khi logout thanh cong.

## 6. API Contract
**Endpoint:** `POST /api/v1/auth/logout`

**Request Body:**
```json
{
  "refresh_token": "rft_abc123..."
}
```

**Response - 200 OK:**
```json
{
  "code": 200,
  "success": true,
  "message": "Dang xuat thanh cong.",
  "data": null,
  "errors": null,
  "timestamp": "2026-05-15T10:00:00Z"
}
```

**Response - 400 Bad Request (payload sai format):**
```json
{
  "code": 400,
  "success": false,
  "message": "Du lieu khong hop le.",
  "data": null,
  "errors": [
    {
      "field": "refresh_token",
      "reason": "REQUIRED"
    }
  ],
  "timestamp": "2026-05-15T10:00:00Z"
}
```

## 7. Validation Rule
| Field | Type | Required | Rules | Error Message |
| :--- | :--- | :--- | :--- | :--- |
| `refresh_token` | string | Yes | Khong rong, dung dinh dang token server phat hanh | "Refresh token khong hop le." |

## 8. Workflow
1. Client gui request logout kem `refresh_token`.
2. Auth Service validate payload (sai -> `400`).
3. Hash refresh token (`sha256`) de tra cuu `REFRESH_TOKEN_SESSION.token_hash`.
4. Neu session ton tai va dang `ACTIVE`:
   - cap nhat status -> `LOGGED_OUT`
   - cap nhat `updated_at`.
5. Neu session khong ton tai hoac da vo hieu hoa:
   - bo qua loi nghiep vu (idempotent).
6. Tra response `200`.

## 9. Database Impact
- Bang chinh tac dong: `REFRESH_TOKEN_SESSION`.
- Cap nhat:
  - `status = LOGGED_OUT` (cho session muc tieu)
  - `updated_at = now()`

Ghi chu:
- Khong bat buoc tao outbox event cho logout theo logic hien tai.

## 10. Error Handling
- `400`: payload khong hop le.
- `200`: logout thanh cong hoac token da bi vo hieu hoa truoc do.
- `500`: loi he thong khong duoc.

## 11. Security
- Khong log raw refresh token.
- Chi su dung `token_hash` khi truy van DB.
- Bat buoc HTTPS/TLS.
- Khuyen nghi gioi han tan suat endpoint logout de tranh abuse.

## 12. FE Behavior
- Khi user nhan "Dang xuat":
  - Goi `POST /api/v1/auth/logout` voi refresh token hien tai.
  - Cho du API ket qua 200 do session da ton tai hay da vo hieu hoa, FE van:
    - xoa access token/refresh token local
    - clear user state
    - redirect ve man hinh login.
- Neu loi he thong 500:
  - FE van xoa local token de dam bao logout phia client
  - thong bao user thu lai neu can dong bo server.

## 13. Acceptance Criteria
- **AC1:** Refresh token hop le cua phien `ACTIVE` -> session chuyen `LOGGED_OUT`, API tra `200`.
- **AC2:** Refresh token da logout/revoked/khong ton tai -> API van tra `200` (idempotent).
- **AC3:** Payload sai (`refresh_token` rong/thieu) -> tra `400`.
- **AC4:** Khong lo token dang plaintext trong logs.

## 14. Mapping to Existing Project Docs
- `docs/business-spec/auth-service-spec.md` (Authentication - Dang xuat)
- `docs/use-cases/uc-user-authentication.md` (muc 3.5 Logout)
- `docs/business-flow/authentication-lifecycle-flow.md`
- `docs/business-flow/session-management-flow.md`
- `docs/database/auth_schema.md` (`REFRESH_TOKEN_SESSION`)
- `docs/engineering-rules/api-standard.md` (response format)
