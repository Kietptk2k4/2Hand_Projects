# Functional Requirement (FR) - Suspend User By Admin

## 1. Feature Overview

**Internal/sync API** tren Auth Service de Admin Service (hoac admin portal qua gateway) ap dung suspend len user: dat trang thai khong cho login va **revoke toan bo refresh sessions**. Admin Service van own enforcement record + audit + outbox `USER_SUSPENDED`; Auth own trang thai tai khoan va session.

La facade cu the cua `FR_ApplyUserEnforcement` cho action `SUSPEND`.

## 2. Actors

- **Admin/Moderator:** Thuc hien suspend (qua Admin UI).
- **Admin Service:** Ghi enforcement, audit, publish event; goi Auth API.
- **Auth Service:** Apply suspend + revoke sessions.

## 3. Scope

- **In Scope:**
  - Nhan `enforcement_id`, `reason_code`, `description`, optional `expires_at`.
  - Kiem tra actor co permission `USER_SUSPEND` (hoac role `ADMIN`/`SUPER_ADMIN`).
  - Set `USERS.status = SUSPENDED` (neu chua).
  - Revoke all `ACTIVE` refresh sessions.
  - Tra `revoked_session_count`.
- **Out of Scope:**
  - Tao ban ghi `user_enforcements` (Admin DB).
  - Ban/restrict (API rieng `/ban`, `/restrict`).
  - Appeal.

## 4. Preconditions

- Actor admin JWT hop le.
- Target user ton tai, khong `DELETED`.
- Admin da tao enforcement `SUSPEND` (hoac tao truoc/sau tuy orchestration — MVP: gui `enforcement_id` de idempotent).

## 5. API Contract

**Endpoint:** `POST /api/v1/admin/users/{userId}/suspend`

**Auth:** Required (admin JWT)

**Path params:**

| Param | Type | Mo ta |
|-------|------|-------|
| `userId` | UUID | User bi suspend |

**Request body:**

```json
{
  "enforcement_id": "uuid-from-admin-service",
  "reason_code": "ABUSE_SPAM",
  "description": "Spam reviews and fake orders",
  "expires_at": "2026-06-01T00:00:00Z"
}
```

**Response - 200 OK:**

```json
{
  "code": 200,
  "success": true,
  "message": "Suspend user thanh cong.",
  "data": {
    "user_id": "uuid",
    "status": "SUSPENDED",
    "revoked_session_count": 3
  },
  "errors": null,
  "timestamp": "2026-05-21T10:00:00Z"
}
```

**Admin Service public API:** `POST /admin/api/v1/users/{userId}/suspend` — orchestration: ghi Admin DB + outbox, goi Auth endpoint tren.

## 6. Business Rules

- `reason_code` va `description` bat buoc (validate Auth hoac trust Admin da validate).
- `expires_at` neu co phai la thoi diem tuong lai (temporary suspend metadata — expiration job Admin + `FR_ApplyUserEnforcement` khi expire).
- User da `SUSPENDED` → idempotent: van revoke sessions neu con ACTIVE, tra success.
- User `DELETED` → `404` `AUTH-404`.
- Actor khong co `USER_SUSPEND` → `403` `AUTH-403`.
- `enforcement_id` dung cho idempotent tracking (log/optional table).
- Khong nhan `user_id` target tu body — chi path.

## 7. Database Impact

- **USERS:** update `status`, `updated_at`.
- **refresh_token_sessions:** update `REVOKED` cho sessions `ACTIVE`.

## 8. Transaction

- User status + session revoke trong 1 transaction.

## 9. Security

- Chi admin co permission.
- Internal call Admin→Auth nen dung service mesh / API key production.
- Ghi `admin_action_logs` phia Admin (khong phai Auth).

## 10. Failure Cases

| HTTP | Code | Khi nao |
|------|------|---------|
| 400 | `AUTH-400` | Thieu reason / expires_at sai |
| 401 | `AUTH-401` | Thieu JWT |
| 403 | `AUTH-403` | Khong du quyen |
| 404 | `AUTH-404` | User khong ton tai / deleted |

## 11. Orchestration with Admin Service

```text
1. Admin UI → Admin API: POST /admin/.../suspend (reason)
2. Admin Service: insert user_enforcements + logs + outbox USER_SUSPENDED
3. Admin Service → Auth: POST /api/v1/admin/users/{id}/suspend
4. Auth: SUSPENDED + revoke sessions
5. Event USER_SUSPENDED → Social/Commerce consume (eventual)
```

## 12. FE Behavior (Admin)

- Form suspend: reason, mo ta, optional ngay het han.
- Xac nhan dialog.
- Hien revoked session count neu API tra ve.

## 13. Acceptance Criteria

- **AC1:** Suspend thanh cong → user khong login lai.
- **AC2:** Tat ca session ACTIVE bi revoke.
- **AC3:** User khong ton tai → 404.
- **AC4:** Actor khong quyen → 403.
- **AC5:** Cung enforcement_id goi lai → idempotent an toan.

## 14. Related

- `FR_ApplyUserEnforcement.md`
- `docs/feature_requirements/admin/FR_SuspendUser.md`
- Implementation: `AdminUserEnforcementController.suspend`, `SuspendUserByAdminUseCase`
