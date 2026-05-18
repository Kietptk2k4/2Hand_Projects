# Functional Requirement (FR) - Thu hoi role khoi user

## 1. Feature Overview
Chuc nang cho phep admin co tham quyen thu hoi role da duoc gan cho user muc tieu de dieu chinh quyen truy cap theo mo hinh RBAC (Role-Based Access Control).

Muc tieu:
- Huy role assignment khi user khong con duoc phep giu vai tro do.
- Dam bao an toan phan quyen: khong cho phep tu thu hoi role cua chinh minh neu policy cam, va bao ve role super admin cuoi cung.

## 2. Actors
- **Super Admin/Admin:** Nguoi thuc hien thao tac thu hoi role.
- **System (Auth Service):** Kiem tra policy domain va cap nhat mapping `USER_ROLES`.

## 3. Scope
- **In Scope:**
  - Thu hoi 1 role cua user theo `user_id` va `role_id`/`role_code`.
  - Validate actor co tham quyen thuc hien.
  - Validate role assignment ton tai truoc khi thu hoi.
  - Ap dung rule bao ve "last super admin" theo policy domain.
  - (Tuy chon theo policy) revoke tat ca session cua user muc tieu de dong bo JWT claims moi.
- **Out of Scope:**
  - Tao/sua/xoa role.
  - Gan permission truc tiep cho user.
  - Tao bo quy tac permission-level chi tiet cho role management UI.

## 4. Preconditions
- Actor da dang nhap hop le.
- Actor co permission quan tri role (vi du: `ASSIGN_ROLE` theo use-case hien tai, hoac `REVOKE_ROLE` neu he thong tach rieng).
- User muc tieu ton tai va khong `DELETED`.
- Role muc tieu ton tai.

## 5. Business Rules
- Khong cho phep actor thu hoi role khi khong du tham quyen.
- Khong cho phep actor tu thu hoi role cua chinh minh neu policy domain cam (`RBAC_SELF_REVOKE_FORBIDDEN`).
- Khong cho phep thu hoi role super admin neu user muc tieu la super admin cuoi cung (`RBAC_LAST_SUPER_ADMIN_PROTECTED`).
- Thu hoi role khong ton tai tren user phai tra ve conflict/phu hop nghiep vu (khong silent success) de FE biet trang thai.
- Sau khi thu hoi role thanh cong, he thong nen dam bao dong bo quyen:
  - hoac yeu cau user muc tieu dang nhap lai,
  - hoac revoke session de bat buoc cap lai JWT claims.
- Tat ca thay doi phan quyen phai duoc audit log.

## 6. API Contract (Target)
**Endpoint:** `DELETE /api/v1/admin/users/{userId}/roles/{roleId}`  
**Auth:** Required (JWT + permission quan tri role)

**Request Body:**  
Khong bat buoc body.

**Response - 200 OK:**
```json
{
  "code": 200,
  "success": true,
  "message": "Thu hoi role khoi user thanh cong.",
  "data": {
    "user_id": "uuid-user-id",
    "role_id": "uuid-role-id"
  },
  "errors": null,
  "timestamp": "2026-05-17T10:00:00Z"
}
```

**Response - 400 Bad Request (du lieu khong hop le):**
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

**Response - 403 Forbidden (khong du quyen/vi pham policy):**
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

**Response - 404 Not Found (user/role khong ton tai):**
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

**Response - 409 Conflict (role chua duoc gan cho user):**
```json
{
  "code": 409,
  "success": false,
  "message": "Resource conflict",
  "data": null,
  "errors": [
    {
      "field": "roleId",
      "reason": "ROLE_NOT_ASSIGNED"
    }
  ],
  "timestamp": "2026-05-17T10:00:00Z"
}
```

## 7. Validation Rule
| Field | Type | Required | Rules | Error Message |
| :--- | :--- | :--- | :--- | :--- |
| `userId` (path) | UUID string | Yes | Dung dinh dang UUID | "Du lieu khong hop le." |
| `roleId` (path) | UUID string | Yes | Dung dinh dang UUID | "Du lieu khong hop le." |
| Actor permission | string | Yes | Co quyen quan tri role | "Access denied" |

## 8. Workflow
1. Admin goi API thu hoi role cua user muc tieu.
2. Auth Service xac thuc actor va check permission quan tri role.
3. Validate `userId`, `roleId`.
4. Kiem tra user ton tai, role ton tai.
5. Kiem tra role assignment cua user:
   - neu chua duoc gan -> `409`.
6. Kiem tra policy domain:
   - khong self-revoke,
   - bao ve last super admin.
7. Delete mapping trong `USER_ROLES`.
8. (Tuy chon) revoke session user muc tieu de buoc refresh claim.
9. Ghi audit/outbox event neu he thong yeu cau.
10. Tra `200`.

## 9. Database Impact
- `USER_ROLES`:
  - Delete `(user_id, role_id)`.
- Read-only check:
  - `USERS`, `ROLES`.
- Rule "last super admin":
  - Dem so user dang co role super admin de kiem tra nguong toi thieu.
- (Tuy chon) `REFRESH_TOKEN_SESSION`:
  - revoke session user muc tieu de nhan claim moi.
- (Tuy chon) `OUTBOX_EVENTS` / audit log.

## 10. Error Handling
- `400`: path param khong hop le.
- `403`: actor khong du permission hoac vi pham policy domain (`self-revoke`, `last super admin protected`).
- `404`: user/role khong ton tai.
- `409`: role chua duoc gan cho user.
- `500`: loi he thong.

## 11. Security
- Endpoint chi cho actor co quyen quan tri role.
- Khong tra thong tin nhay cam cua user muc tieu.
- Ghi audit log day du cho thao tac thu hoi role.
- Bat buoc HTTPS/TLS.

## 12. FE Behavior
- UI admin co man hinh quan ly role cua user.
- Khi admin chon thu hoi role:
  - hien confirm dialog vi day la hanh dong anh huong quyen truy cap.
- Neu `200`:
  - hien toast thanh cong,
  - refresh danh sach role cua user.
- Neu `409`:
  - hien thong bao role khong ton tai tren user (du lieu da thay doi truoc do).
- Neu `403`:
  - hien thong bao khong du quyen hoac vi pham policy.
- Neu `500`:
  - hien retry state.

## 13. Acceptance Criteria
- **AC1:** Admin co quyen thu hoi role da duoc gan cho user ton tai -> `200`.
- **AC2:** Thu hoi role khong ton tai tren user -> `409`.
- **AC3:** Actor khong du quyen -> `403`.
- **AC4:** User/role khong ton tai -> `404`.
- **AC5:** Khong cho phep self-revoke role neu policy cam.
- **AC6:** Khong cho phep thu hoi role super admin neu la super admin cuoi cung.
- **AC7:** Thao tac duoc luu audit log (neu policy yeu cau).

## 14. Current Project Alignment (Hien trang code)
- **Da co o tang domain (nen tang RBAC):**
  - `RoleAssignmentDomainService` da co `ensureCanRevokeRole(...)` va `ensureNotRevokingLastSuperAdmin(...)`.
  - `UserRoleAssignment` da co `revokeRole(...)` va phat `UserRoleRevokedEvent`.
  - `UserRoleAssignmentRepository` da co contract `findByUserId(...)`, `save(...)`, `countUsersByRoleId(...)`.
  - Schema DB da co bang `user_roles` (PK `(user_id, role_id)`), role/permission mapping day du.
- **Chua co o tang application/delivery/infrastructure cho revoke role end-to-end:**
  - Chua co use case `RevokeRoleFromUser`.
  - Chua co controller endpoint `/api/v1/admin/users/{userId}/roles/{roleId}`.
  - Chua thay persistence adapter RBAC cho `UserRoleAssignmentRepository` trong implementation hien tai.
- **Ket luan hien trang:** Nghiep vu domain thu hoi role da co khung rule, nhung chua expose thanh backend API hoan chinh.

## 15. Mapping to Existing Project Docs
- `docs/use-cases/uc-role-permission-management.md` (muc 3.1 Assign/Revoke Role)
- `docs/business-flow/authorization-flow.md`
- `docs/database/auth_schema.md` (`USER_ROLES`, `USERS`, `ROLES`)
- `docs/business-spec/auth-service-spec.md` (Role & Permission - Thu hoi role)
- `docs/Master Specification.md` (Auth Service - Role/Permission scope)
- `docs/engineering-rules/api-standard.md`
