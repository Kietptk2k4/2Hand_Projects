# Functional Requirement (FR) - Dang xuat tat ca phien dang nhap

## 1. Feature Overview
Chuc nang cho phep user da dang nhap chu dong vo hieu hoa toan bo cac phien dang nhap dang hoat dong cua chinh minh de tang cuong bao mat tai khoan.

Muc tieu:
- Cat bo kha nang tiep tuc refresh token tren moi thiet bi sau khi user chon "Logout all".
- Dam bao thao tac theo owner scope: chi tac dong session cua current user.

## 2. Actors
- **User:** Chu tai khoan da dang nhap, muon dang xuat toan bo thiet bi.
- **Client/App:** Web/Mobile goi API logout-all va xu ly state local.

## 3. Scope
- **In Scope:**
  - Tiep nhan yeu cau logout-all cho current user.
  - Revoke toan bo `REFRESH_TOKEN_SESSION` dang `ACTIVE` cua user.
  - Tra ve ket qua thanh cong ke ca khi so session bi revoke = 0 (idempotent theo nghiep vu).
- **Out of Scope:**
  - Logout current session bang refresh token (`POST /api/v1/auth/logout`).
  - Revoke session cua user khac boi admin.
  - Access token blacklist o gateway layer.

## 4. Preconditions
- User da dang nhap hop le (co JWT access token).
- User ton tai trong he thong va khong o trang thai `DELETED`.

## 5. Business Rules
- He thong chi revoke session thuoc `user_id` cua current auth context.
- Chi session dang `ACTIVE` moi bi chuyen `REVOKED`.
- Session da `LOGGED_OUT`, `REVOKED`, `EXPIRED` khong bi thay doi them.
- Neu user khong con session ACTIVE, API van tra `200` (idempotent).
- Client phai xoa auth state local sau khi logout-all thanh cong de dam bao UX va bao mat.

## 6. API Contract (Target)
**Endpoint:** `POST /api/v1/users/me/sessions/logout-all`  
**Auth:** Required (JWT)

**Request Body:**  
Khong bat buoc body.

**Response - 200 OK:**
```json
{
  "code": 200,
  "success": true,
  "message": "Dang xuat tat ca phien dang nhap thanh cong.",
  "data": null,
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
1. Client goi `POST /api/v1/users/me/sessions/logout-all` kem JWT.
2. Auth Service xac dinh `user_id` tu authentication context.
3. Validate auth context va trang thai user.
4. Update `refresh_token_sessions`:
   - `WHERE user_id = :userId AND status = ACTIVE`
   - `SET status = REVOKED, updated_at = now()`
5. Tra `200` (khong phu thuoc so ban ghi bi cap nhat).

## 9. Database Impact
- Bang chinh tac dong: `REFRESH_TOKEN_SESSION` (bang vat ly: `refresh_token_sessions`).
- Cap nhat:
  - `status = REVOKED`
  - `updated_at = CURRENT_TIMESTAMP`
- Dieu kien:
  - `user_id = :currentUserId`
  - `status = ACTIVE`

Ghi chu:
- Theo logic hien tai, khong bat buoc tao outbox event rieng cho logout-all.

## 10. Error Handling
- `401`: thieu/het han/khong hop le access token.
- `200`: thao tac revoke all thanh cong hoac khong co session ACTIVE nao de revoke.
- `500`: loi he thong.

## 11. Security
- Bat buoc JWT auth.
- Owner scope bat buoc (`/users/me/*`).
- Khong log token nhay cam.
- Bat buoc su dung HTTPS/TLS.
- Khuyen nghi bo sung blacklist access token tai gateway neu can "logout sach" ngay lap tuc cho access token chua het han.

## 12. FE Behavior
- Khi user chon "Dang xuat tat ca thiet bi":
  - Goi `POST /api/v1/users/me/sessions/logout-all`.
  - Neu `200`: FE xoa access/refresh token local, clear auth state, redirect login.
- Neu `401`: xu ly theo auth convention (refresh flow), refresh fail thi ve login.
- Neu `500`: hien thong bao loi va cho phep user thu lai.

## 13. Acceptance Criteria
- **AC1:** User da dang nhap goi logout-all -> tat ca session `ACTIVE` cua user chuyen `REVOKED`.
- **AC2:** Session khong `ACTIVE` khong bi thay doi.
- **AC3:** User khong co session `ACTIVE` van nhan `200`.
- **AC4:** Request khong co auth hoac auth khong hop le tra `401`.
- **AC5:** Chi tac dong session cua current user.

## 14. Current Project Alignment (Hien trang code)
- **Da co o tang domain/repository:**
  - `revokeAllByUserId(UUID userId)` trong `RefreshTokenSessionRepository`.
  - SQL adapter cap nhat tat ca session `ACTIVE` -> `REVOKED`.
- **Da duoc su dung noi bo trong use case khac:**
  - `ChangePasswordUseCase` (doi mat khau se revoke all sessions).
  - `SoftDeleteAccountUseCase` (xoa mem tai khoan se revoke all sessions).
- **Chua co o tang delivery/use case rieng:**
  - Chua co endpoint `POST /api/v1/users/me/sessions/logout-all` cho user trigger truc tiep.
  - Chua co FR/API behavior chinh thuc cho logout-all truoc tai lieu nay.
- **Ket luan hien trang:** Nen tang nghiep vu revoke-all da san sang, can expose endpoint va test de hoan tat tinh nang.

## 15. Mapping to Existing Project Docs
- `docs/use-cases/uc-session-management.md` (muc 3.3 Logout all / Revoke)
- `docs/business-flow/session-management-flow.md`
- `docs/database/auth_schema.md` (`REFRESH_TOKEN_SESSION`)
- `docs/feature-requirements/auth/FR_Logout.md`
- `docs/feature-requirements/auth/FR_ChangePassword.md`
- `docs/feature-requirements/auth/FR_SoftDeleteAccount.md`
- `docs/engineering-rules/api-standard.md`
