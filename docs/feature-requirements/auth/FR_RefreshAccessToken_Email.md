# Functional Requirement (FR) - Refresh Access Token (Email Auth)

## 1. Feature Overview
Chuc nang cho phep Client xin cap lai **Access Token** moi khi Access Token cu het han, thong qua **Refresh Token** hop le ma khong can dang nhap lai bang email/password.

Muc tieu:
- Duy tri trai nghiem dang nhap lien tuc.
- Van dam bao an toan phien dang nhap theo session state.

## 2. Actors
- **Client/App:** Ung dung Web/Mobile dang giu refresh token.
- **User:** Chu tai khoan so huu phien dang nhap.

## 3. Scope
- **In Scope:**
  - Tiep nhan refresh token.
  - Kiem tra token hash, trang thai session va han su dung.
  - Cap access token moi.
  - (Tuy chon) cap refresh token moi theo chinh sach rotate.
- **Out of Scope:**
  - Dang nhap email/password.
  - Dang xuat.
  - Revoke by admin.

## 4. Preconditions
- User da login thanh cong truoc do.
- Refresh token ton tai trong `REFRESH_TOKEN_SESSION`.
- Session token co:
  - `status = ACTIVE`
  - `expires_at > now()`
- Tai khoan khong o trang thai `SUSPENDED`/`DELETED`.

## 5. Business Rules
- Access token co TTL ngan (theo cau hinh, vi du 15-30 phut).
- Refresh token co TTL dai hon (theo cau hinh, vi du 7-30 ngay).
- Neu refresh token khong hop le/het han/bi thu hoi => tra `401`, yeu cau login lai.
- Neu session da `REVOKED`/`LOGGED_OUT`/`EXPIRED` => khong cap token moi.
- Khuyen nghi (security hardening):
  - Neu detect refresh token reuse bat thuong khi rotate => revoke toan bo session cua user.

## 6. API Contract
**Endpoint:** `POST /api/v1/auth/refresh`

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
  "message": "Lam moi access token thanh cong.",
  "data": {
    "access_token": "eyJhbGciOi...",
    "expires_in": 1800
  },
  "errors": null,
  "timestamp": "2026-05-15T10:00:00Z"
}
```

**Response - 401 Unauthorized:**
```json
{
  "code": 401,
  "success": false,
  "message": "Phien dang nhap khong hop le hoac da het han. Vui long dang nhap lai.",
  "data": null,
  "errors": null,
  "timestamp": "2026-05-15T10:00:00Z"
}
```

## 7. Validation Rule
| Field | Type | Required | Rules | Error Message |
| :--- | :--- | :--- | :--- | :--- |
| `refresh_token` | string | Yes | Khong rong, dung dinh dang token server phat hanh | "Refresh token khong hop le." |

## 8. Workflow
1. Client gui `refresh_token` den Auth Service.
2. Auth Service validate request format (sai -> `400`).
3. Hash refresh token (`sha256`) de tra cuu `REFRESH_TOKEN_SESSION.token_hash`.
4. Kiem tra session:
   - ton tai
   - `status = ACTIVE`
   - `expires_at > now()`
5. Kiem tra trang thai user (khong `SUSPENDED`/`DELETED`).
6. Tao access token moi.
7. Tra `200` voi access token moi.

## 9. Database Impact
- Read `REFRESH_TOKEN_SESSION` theo `token_hash`.
- Read `USERS` de kiem tra status.
- Khong bat buoc ghi DB trong flow co ban.

Ghi chu mo rong:
- Neu ap dung rotate refresh token, can:
  - cap nhat session cu -> `REVOKED`/`EXPIRED`
  - tao session moi `ACTIVE` voi refresh token moi
  - cac buoc ghi phai nam trong transaction.

## 10. Error Handling
- `400 Bad Request`: payload khong hop le.
- `401 Unauthorized`:
  - token khong ton tai
  - token het han
  - session khong ACTIVE (REVOKED/LOGGED_OUT/EXPIRED)
  - user bi khoa/xoa
- `500 Internal Server Error`: loi he thong.

## 11. Security
- Khong log raw refresh token.
- Chi luu `token_hash` trong DB.
- Bat buoc TLS/HTTPS.
- Gioi han tan suat endpoint refresh de giam abuse.
- Co the ket hop token blacklist tai gateway de chan access token cu khi can.

## 12. FE Behavior
- FE nen goi refresh token theo co che silent refresh khi Access Token sap/da het han.
- Neu nhan `200`: cap nhat access token moi, tiep tuc request truoc do.
- Neu nhan `401`: xoa auth state local, dieu huong ve man hinh login.
- Khong hien popup loi ky thuat cho user neu refresh that bai trong nen; uu tien UX "Session het han, vui long dang nhap lai".

## 13. Acceptance Criteria
- **AC1:** Refresh token hop le + session ACTIVE + chua het han -> tra `200` va access token moi.
- **AC2:** Refresh token khong hop le/het han/bi revoke -> tra `401`.
- **AC3:** User bi `SUSPENDED`/`DELETED` -> khong cap token moi, tra `401`.
- **AC4:** Khong luu/log refresh token dang plaintext.

## 14. Mapping to Existing Docs
- `docs/business-spec/auth-service-spec.md` (Authentication - Refresh Access Token)
- `docs/use-cases/uc-user-authentication.md` (muc 3.4 Refresh Access Token)
- `docs/business-flow/authentication-lifecycle-flow.md`
- `docs/business-flow/session-management-flow.md`
- `docs/database/auth_schema.md` (`REFRESH_TOKEN_SESSION`, `USERS`)
- `docs/engineering-rules/api-standard.md` (response format)
