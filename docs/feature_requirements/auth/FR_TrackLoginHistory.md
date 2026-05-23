# Functional Requirement (FR) - Theo doi lich su dang nhap

## 1. Feature Overview
Chuc nang cho phep user da dang nhap theo doi lich su dang nhap cua chinh minh de phat hien truy cap bat thuong va tang cuong bao mat tai khoan.

Muc tieu:
- Hien thi lich su login theo thu tu moi nhat truoc.
- Bao gom ca lan dang nhap thanh cong va that bai.

## 2. Actors
- **User:** Chu tai khoan da dang nhap, muon xem lich su dang nhap.
- **Client/App:** Web/Mobile goi API va hien thi timeline/bang lich su.

## 3. Scope
- **In Scope:**
  - Lay lich su login theo current user.
  - Ho tro phan trang co ban (`limit`, `offset`).
  - Tra ve thong tin can thiet cho audit UI: `login_method`, `ip_address`, `user_agent`, `success`, `created_at`.
- **Out of Scope:**
  - Lich su session refresh/logout.
  - Phan tich rui ro/geoIP nang cao.
  - Dashboard admin xem lich su cua user khac — xem `FR_ViewLoginHistoryForAdmin.md`.

## 4. Preconditions
- User da dang nhap hop le (co JWT access token).
- He thong da ghi nhan du lieu dang nhap vao `login_logs`.

## 5. Business Rules
- Chi tra lich su thuoc current user trong auth context.
- Du lieu phai sap xep `created_at DESC` (moi nhat truoc).
- Bao gom ca `success = true` va `success = false`.
- Khong tra ve thong tin nhay cam nhu password, token, token_hash.
- Neu chua co lich su dang nhap, tra danh sach rong.
- `limit` va `offset` phai duoc validate de tranh query bat thuong.

## 6. API Contract (Target)
**Endpoint:** `GET /api/v1/users/me/login-history`  
**Auth:** Required (JWT)

**Query Params (de xuat):**
- `limit` (optional, default: 20, max: 100)
- `offset` (optional, default: 0)

**Response - 200 OK:**
```json
{
  "code": 200,
  "success": true,
  "message": "Lay lich su dang nhap thanh cong.",
  "data": {
    "items": [
      {
        "id": "a59411f3-201a-4872-acde-55591524f1a8",
        "login_method": "EMAIL",
        "ip_address": "203.113.10.20",
        "user_agent": "Mozilla/5.0 ...",
        "success": true,
        "created_at": "2026-05-17T09:00:00Z"
      },
      {
        "id": "c96b5e84-c5d7-4620-adce-86bb4e594cd3",
        "login_method": "GOOGLE",
        "ip_address": "10.10.1.25",
        "user_agent": "Chrome/125.0",
        "success": false,
        "created_at": "2026-05-16T07:10:00Z"
      }
    ],
    "limit": 20,
    "offset": 0
  },
  "errors": null,
  "timestamp": "2026-05-17T09:01:00Z"
}
```

**Response - 400 Bad Request (query invalid):**
```json
{
  "code": 400,
  "success": false,
  "message": "Du lieu khong hop le.",
  "data": null,
  "errors": [
    {
      "field": "limit",
      "reason": "INVALID_RANGE"
    }
  ],
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
| `limit` | integer | No | >= 1, <= 100, default 20 | "Limit khong hop le." |
| `offset` | integer | No | >= 0, default 0 | "Offset khong hop le." |

## 8. Workflow
1. Client goi `GET /api/v1/users/me/login-history` kem JWT.
2. Auth Service xac dinh `user_id` tu authentication context.
3. Validate query pagination (`limit`, `offset`).
4. Query `login_logs` theo `user_id`, `ORDER BY created_at DESC`, `LIMIT/OFFSET`.
5. Map du lieu response va tra `200`.

## 9. Database Impact
- Read-only:
  - `LOGIN_LOGS` (bang vat ly: `login_logs`).
- Dieu kien query:
  - `user_id = :currentUserId`
- Sap xep:
  - `ORDER BY created_at DESC`
- Phan trang:
  - `LIMIT :limit OFFSET :offset`

## 10. Error Handling
- `400`: query params khong hop le (`limit`, `offset`).
- `401`: thieu/het han/khong hop le access token.
- `500`: loi he thong trong qua trinh truy van DB/map response.

## 11. Security
- Bat buoc JWT auth.
- Owner scope: chi duoc xem lich su cua chinh minh (`/users/me/*`).
- Khong log token/password.
- IP va user-agent chi dung cho hien thi audit, can che do an du lieu phu hop chinh sach bao mat.
- Bat buoc su dung HTTPS/TLS.

## 12. FE Behavior
- FE goi endpoint khi user vao man hinh "Login History".
- Neu `200`:
  - hien danh sach lich su (co badge thanh cong/that bai).
  - neu rong thi hien empty state.
- Neu `400`:
  - reset pagination ve gia tri hop le (limit/offset mac dinh) va hien thong bao.
- Neu `401`:
  - trigger refresh flow theo convention FE.
  - refresh fail thi clear auth state va redirect login.
- Nen co "load more" hoac pagination controls de tranh tai qua nhieu du lieu mot lan.

## 13. Acceptance Criteria
- **AC1:** User da dang nhap goi API nhan danh sach lich su cua chinh minh.
- **AC2:** Ket qua duoc sap xep moi nhat truoc.
- **AC3:** Bao gom ca ban ghi `success=true` va `success=false`.
- **AC4:** Khong co du lieu thi tra danh sach rong va `200`.
- **AC5:** Auth khong hop le tra `401`.
- **AC6:** `limit/offset` sai tra `400`.

## 14. Current Project Alignment (Hien trang code)
- **Da co o tang domain/repository:**
  - `LoginLogRepository.findByUserId(UUID userId, int limit, int offset)`.
  - SQL adapter query `login_logs` theo `user_id`, `ORDER BY created_at DESC`, co `LIMIT/OFFSET`.
- **Da co du lieu duoc ghi trong login flow:**
  - `LoginUserUseCase` ghi `LOGIN_LOGS` cho dang nhap email (thanh cong + sai mat khau).
  - `OAuthLoginUseCase` ghi `LOGIN_LOGS` cho dang nhap OAuth thanh cong.
- **Chua co o tang delivery/use case rieng:**
  - Chua co endpoint `GET /api/v1/users/me/login-history`.
  - Chua co use case/response DTO rieng cho "track login history".
- **Ket luan hien trang:** Nen tang du lieu va repository da san sang; can expose API + test de hoan tat tinh nang.

## 15. Mapping to Existing Project Docs
- `docs/use-cases/uc-session-management.md` (muc 3.4 View Login History)
- `docs/feature-requirements/auth/FR_Login_Email.md`
- `docs/feature-requirements/auth/FR_Login_OAuth.md`
- `docs/business-flow/session-management-flow.md`
- `docs/database/auth_schema.md` (`LOGIN_LOGS`)
- `docs/engineering-rules/api-standard.md`
- `docs/engineering-rules/frontend-api-integration.md`
