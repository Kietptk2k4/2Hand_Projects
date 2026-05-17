# Functional Requirement (FR) - Xem danh sach role

## 1. Feature Overview
Chuc nang cho phep admin xem danh sach role he thong de ho tro quan tri RBAC va cac thao tac phan quyen (gan/thu hoi role cho user).

Muc tieu:
- Cung cap danh muc role chuan de FE render dropdown/list khi quan tri quyen.
- Dam bao doc du lieu role theo quyen han admin, khong phat sinh thay doi du lieu.

## 2. Actors
- **Admin/Super Admin:** Nguoi xem danh sach role.
- **System (Auth Service):** Truy van va tra ve du lieu role.

## 3. Scope
- **In Scope:**
  - Lay danh sach role tu bang `ROLES`.
  - Tra ve cac truong role co ban: `id`, `code`, `name`, `created_at`, `updated_at`.
  - Sap xep du lieu on dinh de FE de su dung (uu tien `created_at ASC` hoac `code ASC` theo implementation).
- **Out of Scope:**
  - Tao/sua/xoa role.
  - Gan/thu hoi role cho user.
  - Tra chi tiet permission cua tung role (thuoc FR rieng "View permission of role").

## 4. Preconditions
- Actor da dang nhap hop le.
- Actor co quyen quan tri role (theo he thong hien tai co the dung `ASSIGN_ROLE` cho role management).

## 5. Business Rules
- Endpoint chi cho actor co quyen quan tri role.
- Nghiep vu read-only, khong duoc thay doi bat ky du lieu nao.
- Danh sach role tra ve can on dinh de FE cache/tai su dung.
- Khong tra ve thong tin nhay cam ngoai metadata role.
- Neu chua co role nao (truong hop moi khoi tao), tra danh sach rong va `200`.

## 6. API Contract (Target)
**Endpoint:** `GET /api/v1/admin/roles`  
**Auth:** Required (JWT + permission quan tri role)

**Query Params:**  
Khong bat buoc trong MVP.

**Response - 200 OK:**
```json
{
  "code": 200,
  "success": true,
  "message": "Lay danh sach role thanh cong.",
  "data": {
    "roles": [
      {
        "id": "uuid-role-id",
        "code": "ADMIN",
        "name": "Administrator",
        "created_at": "2026-05-17T10:00:00Z",
        "updated_at": "2026-05-17T10:00:00Z"
      }
    ]
  },
  "errors": null,
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

## 7. Validation Rule
| Field | Type | Required | Rules | Error Message |
| :--- | :--- | :--- | :--- | :--- |
| JWT Access Token | string | Yes | Hop le, chua het han | "Authentication required" |
| Actor permission | string | Yes | Co quyen quan tri role | "Access denied" |

## 8. Workflow
1. Admin goi `GET /api/v1/admin/roles` kem JWT.
2. Auth Service xac thuc actor va check permission quan tri role.
3. Query danh sach role tu `ROLES`.
4. Map sang response DTO.
5. Tra `200` voi `data.roles`.

## 9. Database Impact
- Read-only:
  - `ROLES`.
- Khong co ghi/patch/delete.

Ghi chu:
- Cac role mac dinh theo migration hien tai gom `USER`, `ADMIN`, `MODERATOR`.

## 10. Error Handling
- `401`: thieu/het han/khong hop le access token.
- `403`: actor khong du quyen xem role list.
- `500`: loi he thong trong qua trinh query/map du lieu.

## 11. Security
- Bat buoc JWT auth.
- Bat buoc role-management permission.
- Khong expose thong tin ngoai pham vi role metadata.
- Bat buoc HTTPS/TLS.

## 12. FE Behavior
- FE admin goi endpoint khi mo man hinh quan tri role/phan quyen.
- Neu `200`:
  - render danh sach role cho dropdown/list.
  - neu `roles: []`, hien empty state.
- Neu `401`:
  - xu ly refresh-token flow theo convention FE.
  - refresh fail thi clear auth state va redirect login.
- Neu `403`:
  - hien thong bao khong du quyen truy cap chuc nang.
- Neu `500`:
  - hien retry CTA.

## 13. Acceptance Criteria
- **AC1:** Admin co quyen goi API va nhan danh sach role thanh cong.
- **AC2:** Request khong co auth hoac auth khong hop le -> `401`.
- **AC3:** Actor khong du permission role-management -> `403`.
- **AC4:** He thong khong co role van tra `200` voi `roles: []`.
- **AC5:** Response theo dung wrapper trong `api-standard`.

## 14. Current Project Alignment (Hien trang code)
- **Da co nen tang RBAC va du lieu role:**
  - Schema da co bang `roles` va du lieu role mac dinh (`USER`, `ADMIN`, `MODERATOR`).
  - Domain da co `RoleRepository` + `RoleRepositoryAdapter` cho truy van theo `id`/`code`.
  - Cac use case role-management khac (assign/revoke role) da su dung `RoleRepository`.
- **Chua co implementation end-to-end cho view role list:**
  - Chua co use case `ViewRoleList`.
  - Chua co endpoint `GET /api/v1/admin/roles`.
  - `RoleRepository` chua co contract list roles (vd `findAll`/`findAllActive`) cho man hinh role list.
- **Ket luan hien trang:** Nen tang data va repository da co, can bo sung use case + endpoint + query list de hoan tat tinh nang.

## 15. Mapping to Existing Project Docs
- `docs/use-cases/uc-role-permission-management.md` (muc 3.2 Xem danh sach Role va Permission)
- `docs/business-flow/authorization-flow.md`
- `docs/database/auth_schema.md` (`ROLES`)
- `docs/business-spec/auth-service-spec.md` (Role & Permission - Xem danh sach role)
- `docs/Master Specification.md` (Auth Service - Role/Permission scope)
- `docs/engineering-rules/api-standard.md`
