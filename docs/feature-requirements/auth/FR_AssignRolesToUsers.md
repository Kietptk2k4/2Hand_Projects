# Functional Requirement (FR) - Gan role cho user

## 1. Feature Overview
Chuc nang cho phep admin co tham quyen gan role cho user muc tieu de cap quyen truy cap theo mo hinh RBAC (Role-Based Access Control).

Muc tieu:
- Quan tri quyen theo role thay vi cap permission truc tiep tung user.
- Dam bao chinh sach phan quyen an toan (khong tu gan role cho chinh minh neu policy cam).

## 2. Actors
- **Super Admin/Admin:** Nguoi thuc hien thao tac gan role.
- **System (Auth Service):** Kiem tra policy va cap nhat mapping `USER_ROLES`.

## 3. Scope
- **In Scope:**
  - Gan 1 role cho user theo `user_id` va `role_id`/`role_code`.
  - Validate actor co quyen thuc hien.
  - Dam bao khong tao mapping trung lap.
  - (Tuy chon theo policy) revoke session user muc tieu de nhan claim moi sau khi gan role.
- **Out of Scope:**
  - Tao/sua/xoa role.
  - Gan permission truc tiep cho user.
  - UI quan tri role-permission day du.

## 4. Preconditions
- Actor da dang nhap hop le.
- Actor co permission gan role (vi du: `ASSIGN_ROLE`).
- User muc tieu ton tai va khong `DELETED`.
- Role muc tieu ton tai va dang active (neu co trang thai role).

## 5. Business Rules
- Khong cho phep gan role khi actor khong du tham quyen.
- Khong cho phep tao mapping role trung lap cho cung user.
- Khong cho phep actor tu gan role cho chinh minh neu policy he thong cam.
- Sau khi gan role thanh cong, he thong nen dam bao dong bo quyen:
  - hoac yeu cau user muc tieu dang nhap lai,
  - hoac revoke session de JWT moi chua role/permission moi.
- Tat ca thay doi phan quyen phai duoc audit log.

## 6. API Contract (Target)
**Endpoint:** `POST /api/v1/admin/users/{userId}/roles`  
**Auth:** Required (JWT + permission `ASSIGN_ROLE`)

**Request Body:**
```json
{
  "role_id": "uuid-role-id"
}
```

**Response - 200 OK:**
```json
{
  "code": 200,
  "success": true,
  "message": "Gan role cho user thanh cong.",
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
      "field": "role_id",
      "reason": "INVALID_FORMAT"
    }
  ],
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

**Response - 409 Conflict (mapping da ton tai):**
```json
{
  "code": 409,
  "success": false,
  "message": "Resource conflict",
  "data": null,
  "errors": [
    {
      "field": "role_id",
      "reason": "ALREADY_ASSIGNED"
    }
  ],
  "timestamp": "2026-05-17T10:00:00Z"
}
```

## 7. Validation Rule
| Field | Type | Required | Rules | Error Message |
| :--- | :--- | :--- | :--- | :--- |
| `userId` (path) | UUID string | Yes | Dung dinh dang UUID | "Du lieu khong hop le." |
| `role_id` | UUID string | Yes | Dung dinh dang UUID | "Du lieu khong hop le." |
| Actor permission | string | Yes | Phai co `ASSIGN_ROLE` | "Access denied" |

## 8. Workflow
1. Admin goi API gan role cho user muc tieu.
2. Auth Service xac thuc actor va check permission `ASSIGN_ROLE`.
3. Validate `userId`, `role_id`.
4. Kiem tra user ton tai, role ton tai.
5. Kiem tra policy domain:
   - khong self-assign neu bi cam,
   - khong duplicate assignment.
6. Insert mapping vao `USER_ROLES`.
7. (Tuy chon) revoke session user muc tieu de buoc refresh claim.
8. Ghi audit/outbox event neu he thong yeu cau.
9. Tra `200`.

## 9. Database Impact
- `USER_ROLES`:
  - Insert `(user_id, role_id)`.
- Read-only check:
  - `USERS`, `ROLES`.
- (Tuy chon) `REFRESH_TOKEN_SESSION`:
  - revoke session user muc tieu de nhan claim moi.
- (Tuy chon) `OUTBOX_EVENTS` / audit log.

## 10. Error Handling
- `400`: payload/path param khong hop le.
- `403`: actor khong du permission gan role.
- `404`: user/role khong ton tai.
- `409`: role da duoc gan cho user.
- `500`: loi he thong.

## 11. Security
- Endpoint chi cho actor co quyen quan tri role.
- Khong tra thong tin nhay cam cua user muc tieu.
- Ghi audit log day du cho thao tac phan quyen.
- Bat buoc HTTPS/TLS.

## 12. FE Behavior
- UI admin co man hinh chon user + role.
- Truoc khi submit co confirm dialog.
- Neu `200`: hien toast thanh cong + refresh danh sach role cua user.
- Neu `409`: hien thong bao role da duoc gan.
- Neu `403`: hien thong bao khong du quyen.
- Neu `500`: hien retry state.

## 13. Acceptance Criteria
- **AC1:** Admin co quyen gan role thanh cong cho user ton tai.
- **AC2:** Gan role trung lap -> `409`.
- **AC3:** Actor khong du quyen -> `403`.
- **AC4:** User/role khong ton tai -> `404`.
- **AC5:** Thao tac duoc luu audit log (neu policy yeu cau).

## 14. Current Project Alignment (Hien trang code)
- **Da co o tang domain (nen tang RBAC):**
  - `RoleAssignmentDomainService` (co rule domain cho self-assign va bao ve super admin).
  - `UserRoleAssignmentRepository`, `RoleRepository`, `PermissionRepository`, `PermissionQueryRepository` (interface domain).
  - Domain events: `UserRoleAssignedEvent`, `UserRoleRevokedEvent`.
- **Chua co o tang application/delivery/infrastructure:**
  - Chua co use case `AssignRoleToUser`.
  - Chua co controller endpoint `/api/v1/admin/users/{userId}/roles`.
  - Chua thay adapter persistence cho repository RBAC o layer infrastructure.
- **Ket luan hien trang:** Nghiep vu gan role da co khung domain, nhung chua expose thanh backend API hoan chinh.

## 15. Mapping to Existing Project Docs
- `docs/use-cases/uc-role-permission-management.md` (muc 3.1 Assign/Revoke Role)
- `docs/business-flow/authorization-flow.md`
- `docs/database/auth_schema.md` (`USER_ROLES`, `USERS`, `ROLES`)
- `docs/Master Specification.md` (phan Role & Permission)
- `docs/engineering-rules/api-standard.md`
