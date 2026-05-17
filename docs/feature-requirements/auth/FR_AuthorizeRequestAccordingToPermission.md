# Functional Requirement (FR) - Authorize request theo permission

## 1. Feature Overview
Chuc nang cho phep he thong kiem tra quyen (permission) truoc khi cho phep request di vao business logic, dam bao chi actor du quyen moi duoc truy cap endpoint nghiep vu nhay cam.

Muc tieu:
- Ap dung RBAC nhat quan cho cac API can bao ve.
- Tra ve `403 Forbidden` khi actor khong co permission yeu cau.

## 2. Actors
- **System (Auth Service / API Gateway / Security Layer):** Thanh phan thuc hien authorize.
- **Admin/User:** Actor goi API can kiem quyen.

## 3. Scope
- **In Scope:**
  - Xac dinh actor da xac thuc hop le (JWT).
  - Dinh nghia permission yeu cau cho tung endpoint nghiep vu.
  - Kiem tra actor co permission can thiet hay khong.
  - Tu choi request (`403`) neu khong du quyen.
- **Out of Scope:**
  - Tao/sua/xoa role.
  - Gan/thu hoi role cho user.
  - Quan ly danh muc permission.
  - Co che policy phuc tap ABAC (attribute-based access control).

## 4. Preconditions
- Actor da dang nhap hop le va co access token.
- He thong co mapping role-permission trong `ROLES`, `PERMISSIONS`, `ROLE_PERMISSIONS`, `USER_ROLES`.
- Endpoint duoc dinh nghia ro permission can thiet (vi du: `ASSIGN_ROLE`).

## 5. Business Rules
- Request khong co JWT hop le -> `401 Unauthorized`.
- Request co JWT hop le nhung khong du permission -> `403 Forbidden`.
- Request co JWT hop le va du permission -> cho phep di tiep.
- Kiem quyen la read-only, khong thay doi du lieu RBAC.
- Khong de lo thong tin nhay cam trong loi authorize.

## 6. API Contract (Target)
Tinh nang nay la cross-cutting behavior, khong phai 1 endpoint rieng.

**Mau endpoint can authorize (vi du):**
- `GET /api/v1/admin/roles` -> can quyen role-management.
- `POST /api/v1/admin/users/{userId}/roles` -> can `ASSIGN_ROLE`.
- `DELETE /api/v1/admin/users/{userId}/roles/{roleId}` -> can quyen role-management.

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

**Response - 401 Unauthorized (thieu/sai token):**
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

## 7. Validation Rule
| Field | Type | Required | Rules | Error Message |
| :--- | :--- | :--- | :--- | :--- |
| JWT Access Token | string | Yes | Hop le, chua het han | "Authentication required" |
| Required permission | string | Yes | Co trong tap permission cua actor | "Access denied" |

## 8. Workflow
1. Client goi endpoint can bao ve kem JWT.
2. Security layer xac thuc token va set auth context.
3. He thong xac dinh permission can thiet cua endpoint/use case.
4. Lay tap permission cua actor (theo role mapping hoac nguon quyen hien hanh).
5. So khop permission:
   - khong co -> `403`.
   - co -> cho phep tiep tuc business logic.
6. Endpoint tra ket qua nghiep vu binh thuong neu authorize thanh cong.

## 9. Database Impact
- Read-only cho authorize:
  - `USER_ROLES`
  - `ROLE_PERMISSIONS`
  - `PERMISSIONS`
- Khong co ghi/patch/delete cho luong authorize.

## 10. Error Handling
- `401`: token thieu, sai chu ky, het han, auth context khong hop le.
- `403`: actor khong du permission yeu cau.
- `500`: loi he thong trong qua trinh truy van/map permission.

## 11. Security
- Bat buoc HTTPS/TLS.
- Khong log access token thuan van ban.
- Kiem quyen truoc business logic de tranh privilege escalation.
- Tra thong diep loi toi gian, khong lam lo chinh sach bao mat noi bo.

## 12. FE Behavior
- FE luon gui JWT cho endpoint can bao ve.
- Neu `401`:
  - kich hoat refresh flow theo convention.
  - refresh fail thi clear auth state va redirect login.
- Neu `403`:
  - hien thong bao "khong du quyen" va an/disable hanh dong lien quan.
- Khong retry tu dong vo han voi loi `403`.

## 13. Acceptance Criteria
- **AC1:** Request khong co JWT hop le -> `401`.
- **AC2:** Request co JWT hop le nhung khong du permission -> `403`.
- **AC3:** Request co JWT hop le va du permission -> endpoint thuc thi thanh cong.
- **AC4:** Luong authorize khong thay doi du lieu RBAC.
- **AC5:** Response loi authorize theo wrapper chuan `api-standard`.

## 14. Current Project Alignment (Hien trang code)
- **Da co nen tang RBAC va co che check permission o application layer:**
  - Da co `AuthorizationDomainService.hasPermission(...)`.
  - Da co `PermissionQueryRepository.findPermissionCodesByUserId(...)` + adapter SQL join `permissions`, `role_permissions`, `user_roles`.
  - Cac use case role-management (`AssignRolesToUsersUseCase`, `RevokeRoleFromUserUseCase`, `ViewRoleListUseCase`) da check quyen `ASSIGN_ROLE` truoc khi xu ly.
- **Da co xac thuc JWT o security layer:**
  - `JwtAuthenticationFilter` validate token va set `SecurityContext`.
  - `SecurityConfig` bao ve route admin bang `.authenticated()`.
- **Gioi han hien tai so voi muc tieu "authorize theo permission" tong quat:**
  - Chua ap dung co che generic `@PreAuthorize`/`hasAuthority` theo permission cho toan bo endpoint.
  - `JwtTokenProvider` dang map authority tu claim `roles`.
  - `JwtTokenIssuer` hien tai gan claim `roles` co dinh (`USER`) thay vi tap permission claims dong.
  - Nghia la viec authorize theo permission hien dang thuc thi chu yeu trong use case (application layer), chua la permission-enforcement tap trung o filter/interceptor cho moi endpoint.
- **Ket luan hien trang:** Da co mot phan implementation authorize theo permission cho cac nghiep vu RBAC cu the; de hoan tat muc tieu tong quat can bo sung permission-based enforcement tap trung o security layer/gateway.

## 15. Mapping to Existing Project Docs
- `docs/use-cases/uc-role-permission-management.md` (muc 3.3 Authorize Request theo Permission)
- `docs/business-flow/authorization-flow.md`
- `docs/business-spec/auth-service-spec.md` (Role & Permission - Authorize request)
- `docs/database/auth_schema.md` (`USER_ROLES`, `ROLE_PERMISSIONS`, `PERMISSIONS`)
- `docs/engineering-rules/api-standard.md`
- `docs/feature-requirements/auth/FR_AssignRolesToUsers.md`
- `docs/feature-requirements/auth/FR_ViewRoleList.md`
