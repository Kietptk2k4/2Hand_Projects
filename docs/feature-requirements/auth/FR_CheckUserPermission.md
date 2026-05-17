# Functional Requirement (FR) - Kiem tra permission cua user

## 1. Feature Overview
Chuc nang cho phep admin hoac he thong lay danh sach permission hieu luc cua mot user tai thoi diem hien tai, duoc aggregate tu cac role dang gan cho user do.

Muc tieu:
- Ho tro kiem tra quyen thuc te cua user de phuc vu quan tri, audit, va troubleshoot quyen truy cap.
- Cung cap du lieu permission de FE/admin co the hien thi ro user dang co nhung quyen nao.

## 2. Actors
- **Admin/Super Admin:** Nguoi thuc hien thao tac kiem tra permission cua user.
- **System (Auth Service):** Thanh phan aggregate permission tu role mapping.

## 3. Scope
- **In Scope:**
  - Lay tap permission code cua 1 user theo `userId`.
  - Aggregate permission tu `USER_ROLES` + `ROLE_PERMISSIONS` + `PERMISSIONS`.
  - Loai bo phan tu trung lap trong danh sach permission.
  - Nghiep vu read-only.
- **Out of Scope:**
  - Gan/thu hoi role.
  - Gan/thu hoi permission cho role.
  - Authorize request realtime cho tung endpoint (thuoc FR rieng).
  - Chinh sua metadata permission catalog.

## 4. Preconditions
- Actor da dang nhap hop le.
- Actor co quyen quan tri role/permission (theo logic hien tai co the su dung `ASSIGN_ROLE` cho nhom role-management).
- User muc tieu ton tai va khong `DELETED`.

## 5. Business Rules
- Chi actor co quyen quan tri moi duoc check permission cua user khac.
- Ket qua la tap permission hop nhat tu tat ca role hien co cua user.
- Permission trung lap phai duoc loai bo.
- Neu user ton tai nhung chua co role/permission nao -> tra danh sach rong va `200`.
- Neu user khong ton tai -> `404`.

## 6. API Contract (Target)
**Endpoint:** `GET /api/v1/admin/users/{userId}/permissions`  
**Auth:** Required (JWT + permission quan tri role/permission)

**Request Body:**  
Khong bat buoc body.

**Response - 200 OK:**
```json
{
  "code": 200,
  "success": true,
  "message": "Lay danh sach permission cua user thanh cong.",
  "data": {
    "user_id": "uuid-user-id",
    "permissions": [
      {
        "code": "ASSIGN_ROLE"
      },
      {
        "code": "USER_UPDATE"
      }
    ]
  },
  "errors": null,
  "timestamp": "2026-05-17T10:00:00Z"
}
```

**Response - 400 Bad Request (path param khong hop le):**
```json
{
  "code": 400,
  "success": false,
  "message": "Du lieu khong hop le.",
  "data": null,
  "errors": [
    {
      "field": "userId",
      "reason": "INVALID_FORMAT"
    }
  ],
  "timestamp": "2026-05-17T10:00:00Z"
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
  "timestamp": "2026-05-17T10:00:00Z"
}
```

**Response - 403 Forbidden (khong du quyen):**
```json
{
  "code": 403,
  "success": false,
  "message": "Access denied",
  "data": null,
  "errors": null,
  "timestamp": "2026-05-17T10:00:00Z"
}
```

**Response - 404 Not Found (user khong ton tai):**
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
| JWT Access Token | string | Yes | Hop le, chua het han | "Authentication required" |
| Actor permission | string | Yes | Co quyen role/permission management | "Access denied" |

## 8. Workflow
1. Admin goi `GET /api/v1/admin/users/{userId}/permissions` kem JWT.
2. Auth Service xac thuc actor va check permission role-management.
3. Validate `userId`.
4. Kiem tra user muc tieu ton tai va khong `DELETED`.
5. Query permission codes theo `userId` thong qua role mapping.
6. Remove duplicate permissions.
7. Tra `200` voi danh sach permission.

## 9. Database Impact
- Read-only:
  - `USERS` (xac nhan user ton tai/trang thai).
  - `USER_ROLES`.
  - `ROLE_PERMISSIONS`.
  - `PERMISSIONS`.
- Khong co thao tac ghi du lieu.

## 10. Error Handling
- `400`: `userId` sai dinh dang.
- `401`: thieu/het han/khong hop le access token.
- `403`: actor khong du quyen kiem tra permission cua user.
- `404`: user khong ton tai hoac `DELETED`.
- `500`: loi he thong trong qua trinh query/map du lieu.

## 11. Security
- Bat buoc JWT auth.
- Bat buoc permission role-management.
- Khong tra thong tin nhay cam ngoai danh sach permission.
- Bat buoc HTTPS/TLS.

## 12. FE Behavior
- FE admin goi endpoint khi can xem quyen thuc te cua 1 user.
- Neu `200`:
  - hien thi danh sach permission cua user.
  - neu rong thi hien empty state "User chua co permission nao."
- Neu `400`:
  - hien thong bao request khong hop le.
- Neu `401`:
  - xu ly refresh-token flow theo convention.
  - refresh fail thi clear auth state va redirect login.
- Neu `403`:
  - hien thong bao khong du quyen truy cap.
- Neu `404`:
  - hien thong bao user khong ton tai/da bi xoa.
- Neu `500`:
  - hien retry CTA.

## 13. Acceptance Criteria
- **AC1:** Admin co quyen xem duoc tap permission cua user ton tai.
- **AC2:** User ton tai nhung chua co role/permission -> `200` voi danh sach rong.
- **AC3:** `userId` sai dinh dang -> `400`.
- **AC4:** Request khong co auth hoac auth khong hop le -> `401`.
- **AC5:** Actor khong du permission -> `403`.
- **AC6:** User khong ton tai hoac da `DELETED` -> `404`.
- **AC7:** Response theo dung wrapper trong `api-standard`.

## 14. Current Project Alignment (Hien trang code)
- **Da co nen tang aggregate permission cua user:**
  - `PermissionQueryRepository` da co method `findPermissionCodesByUserId(UUID userId)`.
  - `PermissionQueryRepositoryAdapter` da join `permissions`, `role_permissions`, `user_roles` va tra tap permission codes distinct.
  - Cac use case role-management hien tai da dung method nay de check quyen actor (`ASSIGN_ROLE`).
- **Da co nen tang xac thuc/auth context:**
  - `JwtAuthenticationFilter` + `SecurityConfig` da bao ve route admin bang `.authenticated()`.
  - Controller admin da co pattern parse `userId` va tra loi validation/auth theo convention.
- **Chua co implementation end-to-end cho use case "CheckUserPermission" rieng:**
  - Chua co use case `CheckUserPermission`.
  - Chua co endpoint `GET /api/v1/admin/users/{userId}/permissions`.
  - Chua co response DTO rieng cho "permission list by user".
- **Ket luan hien trang:** Nen tang du lieu/query da san sang, can bo sung use case + endpoint + response mapping de expose tinh nang day du.

## 15. Mapping to Existing Project Docs
- `docs/use-cases/uc-role-permission-management.md` (muc 3.5 Kiem tra permission cua user)
- `docs/business-spec/auth-service-spec.md` (Role & Permission - Kiem tra permission cua user)
- `docs/database/auth_schema.md` (`USER_ROLES`, `ROLE_PERMISSIONS`, `PERMISSIONS`, `USERS`)
- `docs/business-flow/authorization-flow.md`
- `docs/feature-requirements/auth/FR_AuthorizeRequestAccordingToPermission.md`
- `docs/engineering-rules/api-standard.md`
