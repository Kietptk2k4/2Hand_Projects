# Functional Requirement (FR) - Xem danh sach phien dang nhap

## 1. Feature Overview
Chuc nang cho phep user da dang nhap xem danh sach cac phien dang nhap cua chinh minh de kiem soat thiet bi va tang cuong bao mat.

Muc tieu:
- Hien thi danh sach session dang con hieu luc cua user.
- Ho tro flow quan ly session (logout current/logout all) o cac FR lien quan.

## 2. Actors
- **User:** Chu tai khoan da dang nhap, muon xem cac phien dang nhap cua minh.
- **Client/App:** Web/Mobile goi API va hien thi danh sach session.

## 3. Scope
- **In Scope (muc tieu nghiep vu):**
  - Lay danh sach session theo user hien tai.
  - Tra ve thong tin nhan dien session: `device_id`, `ip_address`, `user_agent`, `created_at`, `updated_at`, `status`.
  - Sap xep session moi nhat truoc.
- **Out of Scope:**
  - Logout session cu the.
  - Logout all sessions.
  - Login history (du lieu tu `LOGIN_LOGS`).

## 4. Preconditions
- User da dang nhap hop le (co JWT access token).
- He thong co du lieu session trong `refresh_token_sessions`.

## 5. Business Rules
- Chi tra session thuoc ve current user trong auth context.
- Theo use case session management cua do an, man hinh "danh sach phien dang nhap" uu tien hien thi session `ACTIVE`.
- Session duoc sap xep giam dan theo `created_at`.
- Khong bao gio tra ve `token_hash` hoac bat ky token dang plaintext.
- Neu user khong co session ACTIVE, tra danh sach rong.

## 6. API Contract (Target)
**Endpoint:** `GET /api/v1/users/me/sessions`  
**Auth:** Required (JWT)

**Response - 200 OK:**
```json
{
  "code": 200,
  "success": true,
  "message": "Lay danh sach phien dang nhap thanh cong.",
  "data": {
    "sessions": [
      {
        "id": "9cfadc7f-4aa7-4917-a076-d8a5e8bb4be6",
        "device_id": "web-chrome-win11",
        "ip_address": "203.113.10.20",
        "user_agent": "Mozilla/5.0 ...",
        "status": "ACTIVE",
        "created_at": "2026-05-17T09:00:00Z",
        "updated_at": "2026-05-17T09:00:00Z",
        "expires_at": "2026-06-16T09:00:00Z"
      }
    ]
  },
  "errors": null,
  "timestamp": "2026-05-17T09:01:00Z"
}
```

**Response - 401 Unauthorized:**
```json
{
  "code": 401,
  "success": false,
  "message": "Authentication required",
  "data": null,
  "errors": null,
  "timestamp": "2026-05-17T09:01:00Z"
}
```

## 7. Validation Rule
| Field | Type | Required | Rules | Error Message |
| :--- | :--- | :--- | :--- | :--- |
| JWT Access Token | string | Yes | Hop le, chua het han | "Authentication required" |

## 8. Workflow
1. Client goi `GET /api/v1/users/me/sessions` kem JWT.
2. Auth Service xac dinh `user_id` tu authentication context.
3. Query session repository theo `user_id` va `status = ACTIVE`.
4. Sap xep ket qua theo `created_at DESC`.
5. Map du lieu response, loai bo thong tin nhay cam (`token_hash`).
6. Tra `200` voi danh sach sessions.

## 9. Database Impact
- Read-only:
  - `REFRESH_TOKEN_SESSION` (bang vat ly: `refresh_token_sessions`).
- Dieu kien query:
  - `user_id = :currentUserId`
  - `status = ACTIVE`
- Sap xep:
  - `ORDER BY created_at DESC`

## 10. Error Handling
- `401`: thieu/het han/khong hop le access token.
- `500`: loi he thong trong qua trinh truy van DB hoac map response.

## 11. Security
- Bat buoc JWT auth.
- Owner scope: chi duoc xem session cua chinh minh (`/users/me/*`).
- Khong tra ve `token_hash` va khong log token.
- Bat buoc su dung HTTPS/TLS.

## 12. FE Behavior
- FE goi endpoint sau khi user vao man hinh Security/Session.
- Neu `200`:
  - hien danh sach thiet bi dang dang nhap.
  - neu danh sach rong thi hien empty state.
- Neu `401`:
  - trigger auth flow refresh token theo convention FE.
  - neu refresh fail: clear session local va redirect login.

## 13. Acceptance Criteria
- **AC1:** User da dang nhap goi API thanh cong nhan danh sach session thuoc ve minh.
- **AC2:** Response khong bao gom `token_hash` hoac du lieu token nhay cam.
- **AC3:** Session trong response duoc sap xep moi nhat truoc.
- **AC4:** User khong co session ACTIVE nhan `sessions: []` va `200`.
- **AC5:** Request khong co auth hoac auth khong hop le tra `401`.

## 14. Current Project Alignment (Hien trang code)
- **Da co o tang domain/repository:**
  - `findByUserIdAndStatus(UUID userId, SessionStatus status)` trong `RefreshTokenSessionRepository`.
  - SQL adapter query `refresh_token_sessions` theo `user_id` + `status`, `ORDER BY created_at DESC`.
- **Chua co o tang delivery/use case:**
  - Chua co endpoint `GET /api/v1/users/me/sessions`.
  - Chua co use case/response DTO rieng cho "view login session list".
- **Ket luan hien trang:** Chuc nang nay da co nen tang du lieu, nhung chua expose API de frontend su dung.

## 15. Mapping to Existing Project Docs
- `docs/use-cases/uc-session-management.md` (muc 3.1 View Sessions)
- `docs/business-flow/session-management-flow.md`
- `docs/database/auth_schema.md` (`REFRESH_TOKEN_SESSION`)
- `docs/engineering-rules/api-standard.md` (response wrapper)
- `docs/engineering-rules/frontend-api-integration.md` (auth + `/api/v1/users/me/*`)
