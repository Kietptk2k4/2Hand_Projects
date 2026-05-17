# Functional Requirement (FR) - Xem permission cua role

## 1. Feature Overview
Chuc nang cho phep admin xem danh sach permission gan voi mot role cu the de kiem tra cau hinh quyen va ho tro cac luong quan tri RBAC.

Muc tieu:
- Minh bach hoa tap permission cua tung role trong he thong.
- Ho tro UI quan tri role/permission va cac quy trinh audit quyen truy cap.

## 2. Actors
- **Admin/Super Admin:** Nguoi xem permission cua role.
- **System (Auth Service):** Truy van role va mapping permission de tra ve ket qua.

## 3. Scope
- **In Scope:**
  - Lay danh sach permission cua 1 role theo `roleId`.
  - Tra ve metadata role co ban va danh sach permission codes.
  - Nghiep vu read-only, khong thay doi role/permission mapping.
- **Out of Scope:**
  - Tao/sua/xoa role.
  - Gan/thu hoi role cho user.
  - Gan/thu hoi permission cho role.
  - API lay toan bo permission catalog doc lap (khong theo role).

## 4. Preconditions
- Actor da dang nhap hop le.
- Actor co quyen quan tri role/permission (theo logic hien tai co the su dung `ASSIGN_ROLE` cho nhom role-management).
- Role muc tieu ton tai.

## 5. Business Rules
- Endpoint chi cho actor co quyen quan tri role.
- Neu role khong ton tai -> tra `404`.
- Neu role ton tai nhung chua co permission nao -> tra `200` voi danh sach rong.
- Du lieu tra ve phai read-only va khong lo thong tin nhay cam.
- Response phai theo wrapper chuan `api-standard`.

## 6. API Contract (Target)
**Endpoint:** `GET /api/v1/admin/roles/{roleId}/permissions`  
**Auth:** Required (JWT + permission quan tri role)

**Request Body:**  
Khong bat buoc body.

**Response - 200 OK:**
```json
{
  "code": 200,
  "success": true,
  "message": "Lay danh sach permission cua role thanh cong.",
  "data": {
    "role": {
      "id": "uuid-role-id",
      "code": "ADMIN",
      "name": "Administrator"
    },
    "permissions": [
      {
        "code": "USER_READ"
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
      "field": "roleId",
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

**Response - 404 Not Found (role khong ton tai):**
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
| `roleId` (path) | UUID string | Yes | Dung dinh dang UUID | "Du lieu khong hop le." |
| JWT Access Token | string | Yes | Hop le, chua het han | "Authentication required" |
| Actor permission | string | Yes | Co quyen quan tri role | "Access denied" |

## 8. Workflow
1. Admin goi `GET /api/v1/admin/roles/{roleId}/permissions` kem JWT.
2. Auth Service xac thuc actor va check permission role-management.
3. Validate `roleId`.
4. Query role theo `roleId`; khong ton tai -> `404`.
5. Query permission cua role tu mapping `ROLE_PERMISSIONS` join `PERMISSIONS`.
6. Map response DTO gom metadata role + danh sach permission.
7. Tra `200`.

## 9. Database Impact
- Read-only:
  - `ROLES` (xac thuc role ton tai).
  - `ROLE_PERMISSIONS` + `PERMISSIONS` (lay danh sach permission cua role).
- Khong co thao tac ghi du lieu.

## 10. Error Handling
- `400`: `roleId` khong hop le.
- `401`: thieu/het han/khong hop le access token.
- `403`: actor khong du quyen xem permission cua role.
- `404`: role khong ton tai.
- `500`: loi he thong trong qua trinh query/map du lieu.

## 11. Security
- Bat buoc JWT auth.
- Bat buoc quyen quan tri role/permission.
- Chi expose thong tin can thiet (`role` metadata, `permission code`).
- Bat buoc HTTPS/TLS.

## 12. FE Behavior
- FE admin goi endpoint khi user chon 1 role de xem chi tiet permission.
- Neu `200`:
  - render danh sach permission cua role.
  - neu rong thi hien empty state "Role chua co permission."
- Neu `400`:
  - hien thong bao du lieu vao khong hop le.
- Neu `401`:
  - xu ly refresh-token flow theo convention FE.
  - refresh fail thi clear auth state va redirect login.
- Neu `403`:
  - hien thong bao khong du quyen truy cap.
- Neu `404`:
  - thong bao role khong ton tai/da bi xoa.
- Neu `500`:
  - hien retry CTA.

## 13. Acceptance Criteria
- **AC1:** Admin co quyen goi API va nhan danh sach permission cua role ton tai.
- **AC2:** Role ton tai nhung chua co permission -> tra `200` voi `permissions: []`.
- **AC3:** `roleId` sai dinh dang -> `400`.
- **AC4:** Request khong co auth hoac auth khong hop le -> `401`.
- **AC5:** Actor khong du quyen -> `403`.
- **AC6:** Role khong ton tai -> `404`.
- **AC7:** Response theo dung wrapper trong `api-standard`.

## 14. Current Project Alignment (Hien trang code)
- **Da co nen tang du lieu va query permission:**
  - Schema da co `permissions` va `role_permissions`.
  - `PermissionQueryRepository` da co query permission code theo nhom `roleIds`.
  - `PermissionQueryRepositoryAdapter` da join `permissions` + `role_permissions`.
  - `RoleRepository` + `RoleRepositoryAdapter` da truy van role theo `id`/`code`.
- **Chua co implementation end-to-end cho view permissions of role:**
  - Chua co use case `ViewPermissionsOfRole`.
  - Chua co endpoint `GET /api/v1/admin/roles/{roleId}/permissions`.
  - `PermissionQueryRepository` chua co method truy van truc tiep theo mot `roleId` (dang qua nhom `roleIds`).
  - Chua co response DTO rieng cho man hinh xem permission cua role.
- **Ket luan hien trang:** Nen tang schema/repository da san sang, can bo sung use case + endpoint + response mapping de expose tinh nang day du.

## 15. Mapping to Existing Project Docs
- `docs/use-cases/uc-role-permission-management.md` (muc 3.2 Xem danh sach Role va Permission)
- `docs/business-spec/auth-service-spec.md` (Role & Permission - Xem permission cua role)
- `docs/business-flow/authorization-flow.md`
- `docs/database/auth_schema.md` (`ROLES`, `PERMISSIONS`, `ROLE_PERMISSIONS`)
- `docs/feature-requirements/auth/FR_ViewRoleList.md`
- `docs/engineering-rules/api-standard.md`
