# Functional Requirement (FR) - Xem public profile user

## 1. Feature Overview
Cho phep guest hoac user khac xem thong tin public profile cua mot user muc tieu.
Feature nay can ton trong co che privacy (`is_private`) de an/lo cac truong nhay cam tren profile.

Muc tieu:
- Ho tro hien thi profile cong khai cho trang user/profile card.
- Dong bo voi quy tac privacy trong `USER_PROFILES.is_private`.

## 2. Actors
- **Guest/Other User:** Nguoi xem profile cua user khac.
- **System (Auth Service):** Cung cap du lieu profile cong khai theo policy privacy.

## 3. Scope
- **In Scope:**
  - Lay public profile theo `user_id` cua user muc tieu.
  - Kiem tra `is_private` va an/lo truong du lieu theo policy.
  - Tra ve cac field public an toan de FE render profile.
- **Out of Scope:**
  - Cap nhat profile/privacy.
  - Xem du lieu tai khoan noi bo (`email`, `status`, `settings`) cua user khac.
  - Authorization theo role admin cho profile private (policy rieng neu can).

## 4. Preconditions
- User muc tieu ton tai va khong o trang thai `DELETED`.
- Ho so `USER_PROFILES` cua user muc tieu ton tai.

## 5. Business Rules
- Neu `is_private = false`:
  - tra `display_name`, `avatar_url`, `bio`, `website`, `social_links`.
- Neu `is_private = true`:
  - chi tra `display_name`, `avatar_url`;
  - cac truong `bio`, `website`, `social_links` phai an (null/empty theo contract).
- Khong bao gio tra ve du lieu nhay cam:
  - `email`, `password_hash`, token, session data.
- Request profile cua user da `DELETED` phai tra `404`.

## 6. API Contract (Target)
**Endpoint:** `GET /api/v1/users/{userId}/public-profile`  
**Auth:** Optional (co the cho phep guest, policy hien tai uu tien public read)

**Response - 200 OK (profile public):**
```json
{
  "code": 200,
  "success": true,
  "message": "Lay public profile thanh cong.",
  "data": {
    "user_id": "uuid",
    "display_name": "Kiet Tran",
    "avatar_url": "https://minio.example.com/2hands-avatar/u1.png",
    "bio": "Backend engineer",
    "website": "https://example.com",
    "social_links": {
      "github": "https://github.com/kiet"
    },
    "is_private": false
  },
  "errors": null,
  "timestamp": "2026-05-17T10:00:00Z"
}
```

**Response - 200 OK (profile private):**
```json
{
  "code": 200,
  "success": true,
  "message": "Lay public profile thanh cong.",
  "data": {
    "user_id": "uuid",
    "display_name": "Kiet Tran",
    "avatar_url": "https://minio.example.com/2hands-avatar/u1.png",
    "bio": null,
    "website": null,
    "social_links": {},
    "is_private": true
  },
  "errors": null,
  "timestamp": "2026-05-17T10:00:00Z"
}
```

**Response - 404 Not Found:**
```json
{
  "code": 404,
  "success": false,
  "message": "Resource not found",
  "data": null,
  "errors": null,
  "timestamp": "2026-05-17T10:00:00Z"
}
```

## 7. Validation Rule
| Field | Type | Required | Rules | Error Message |
| :--- | :--- | :--- | :--- | :--- |
| `userId` (path) | UUID string | Yes | Dung dinh dang UUID | "Du lieu khong hop le." |

## 8. Workflow
1. Client goi `GET /api/v1/users/{userId}/public-profile`.
2. Auth Service validate `userId` path param.
3. Query `USERS` + `USER_PROFILES` cua user muc tieu.
4. Neu user khong ton tai hoac `status = DELETED` -> tra `404`.
5. Kiem tra `is_private`:
   - `false`: tra day du profile public fields.
   - `true`: an `bio`, `website`, `social_links`.
6. Tra `200` theo response wrapper chuan.

## 9. Database Impact
- Read-only:
  - `USERS` (kiem tra ton tai/status).
  - `USER_PROFILES` (lay public profile + privacy flag).

## 10. Error Handling
- `400`: `userId` khong hop le.
- `404`: user/profile khong ton tai hoac user da `DELETED`.
- `500`: loi he thong.

## 11. Security
- Khong expose thong tin nhay cam cua user muc tieu.
- Ton trong privacy flag `is_private`.
- Bat buoc HTTPS/TLS.
- Khuyen nghi rate limit read endpoint neu bi crawl bat thuong.

## 12. FE Behavior
- FE co the goi endpoint khi mo trang profile cong khai.
- Neu `200`:
  - render `display_name`, `avatar_url` luon co.
  - neu `is_private = true`, hien thong bao "Tai khoan dang o che do rieng tu" va an section chi tiet.
- Neu `404`: hien "Khong tim thay nguoi dung".
- Neu `500`: hien retry state.

## 13. Acceptance Criteria
- **AC1:** User muc tieu `is_private=false` -> tra day du fields public.
- **AC2:** User muc tieu `is_private=true` -> chi tra truong cho phep (`display_name`, `avatar_url`).
- **AC3:** User `DELETED` hoac khong ton tai -> `404`.
- **AC4:** Khong co truong nhay cam nao duoc expose.

## 14. Current Project Alignment (Hien trang code)
- **Da co o tang domain/repository:**
  - `UserProfileRepository.findByUserId(...)`.
  - field `is_private` trong `USER_PROFILES` va domain `UserProfile`.
  - Use case self-view (`ViewAccountUseCase`) da doc du lieu users/profile/settings.
- **Da co nghiep vu privacy lien quan:**
  - endpoint `PATCH /api/v1/users/me/privacy` de toggle `is_private`.
- **Chua co o tang delivery/use case cong khai:**
  - chua co endpoint public profile (`/api/v1/users/{userId}/public-profile` hoac tuong duong).
  - chua co use case rieng xu ly che do an/hien fields theo `is_private`.
- **Ket luan hien trang:** Nen tang du lieu/public-field da san sang; can them API public profile de hoan tat feature.

## 15. Mapping to Existing Project Docs
- `docs/use-cases/uc-user-profile-management.md` (muc 3.1 Main Flow Public)
- `docs/business-flow/profile-privacy-flow.md`
- `docs/feature-requirements/auth/FR_ViewAccount.md`
- `docs/feature-requirements/auth/FR_TogglePrivateProfile.md`
- `docs/feature-requirements/auth/FR_UpdateProfile.md`
- `docs/database/auth_schema.md` (`USERS`, `USER_PROFILES`)
- `docs/engineering-rules/api-standard.md`
