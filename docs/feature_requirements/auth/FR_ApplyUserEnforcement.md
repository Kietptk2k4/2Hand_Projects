# Functional Requirement (FR) - Apply User Enforcement

## 1. Feature Overview

Auth Service ap dung **hieu ung enforcement** len tai khoan user khi Admin Service (hoac he thong) ra quyet dinh suspend/ban/restrict/revoke/expire. Day la **consumer/apply side** — bo sung cho viec Admin luu enforcement trong Admin DB va publish event.

Hai kenh tich hop MVP (co the dung song song):

1. **Synchronous internal API** — vi du `FR_SuspendUserByAdmin` (Admin goi Auth ngay).
2. **Event-driven** — consume `USER_SUSPENDED`, `USER_BANNED`, `USER_RESTRICTED`, `USER_ENFORCEMENT_REVOKED`, `USER_ENFORCEMENT_EXPIRED` tu broker (payload chua `enforcement_id`, `user_id`, `action_type`).

FR nay dinh nghia **logic apply** chung; cac API cu the (`FR_SuspendUserByAdmin`, ...) la facade.

## 2. Actors

- **Admin Service:** Publish event hoac goi internal API.
- **Auth Service:** Apply `USERS.status`, revoke sessions, optional restriction flags.
- **Downstream (Social/Commerce):** Consume cung event (ngoai scope Auth FR).

## 3. Scope

- **In Scope:**
  - Apply mapping enforcement → user state Auth:
    - `SUSPEND` / `BAN` → `USERS.status = SUSPENDED`, revoke all refresh sessions.
    - `RESTRICT` → user van `ACTIVE` login; luu restriction policy snapshot (cache/DB) cho Auth check write APIs (neu co).
    - `REVOKE` / `EXPIRE` enforcement → restore `ACTIVE` neu khong con enforcement ACTIVE khac.
  - Idempotent theo `enforcement_id` (khong apply trung).
  - Ghi audit log noi bo Auth (optional `enforcement_applied_logs` hoac reuse login/session log).
- **Out of Scope:**
  - Luu enforcement record chinh (`user_enforcements`) — Admin Service own.
  - Appeal workflow.
  - Commerce/Social apply (service owner).

## 4. Preconditions

- Payload hop le tu Admin (signed internal call hoac trusted event).
- Target `user_id` ton tai trong `USERS`.

## 5. Integration Contract

### 5.1 Event payload (consume — khuyen nghi)

```json
{
  "event_id": "uuid",
  "event_type": "USER_SUSPENDED",
  "enforcement_id": "uuid",
  "user_id": "uuid",
  "action_type": "SUSPEND",
  "reason_code": "ABUSE_REPORT",
  "expires_at": null,
  "occurred_at": "2026-05-21T10:00:00Z"
}
```

### 5.2 Internal API (sync — da co facade)

- `POST /api/v1/admin/users/{userId}/suspend` → `FR_SuspendUserByAdmin`
- `POST /api/v1/admin/users/{userId}/ban`
- `POST /api/v1/admin/users/{userId}/restrict`
- `POST /api/v1/admin/user-enforcements/{enforcementId}/revoke`

## 6. Business Rules

| Action | Auth user status | Sessions | Login |
|--------|------------------|----------|-------|
| `SUSPEND` | `SUSPENDED` | Revoke all `ACTIVE` | Block |
| `BAN` | `SUSPENDED` (MVP) hoac `BANNED` neu co enum | Revoke all | Block |
| `RESTRICT` | `ACTIVE` | Khong bat buoc revoke | Allowed; chan write tuy policy |
| `REVOKE` / `EXPIRE` | `ACTIVE` neu khong con enforcement khac | Khong tu tao session moi | Allowed |

- Idempotent: cung `enforcement_id` + `event_id` → skip hoac no-op.
- `DELETED` user → ignore hoac 404 tuy API.
- Khong xoa user vat ly.
- Sync API yeu cau actor admin JWT + permission (`USER_SUSPEND`, ...).
- Event consumer dung service account, verify schema version.

## 7. Database Impact

- **USERS:** update `status`, `updated_at`.
- **refresh_token_sessions:** bulk revoke khi suspend/ban.
- Optional: **user_enforcement_snapshots** (Auth local cache) cho RESTRICT flags — MVP co the chi rely event to Social/Commerce.

## 8. Transaction

- Update user + revoke sessions trong 1 transaction khi suspend/ban.
- Consumer commit truoc khi ack message (at-least-once → retry safe).

## 9. Security

- Internal API chi chap nhan tu Admin Service network / mTLS / shared secret (production).
- Event consumer xac thuc topic + schema.
- Khong nhan `user_id` tu public client de suspend.

## 10. Failure Cases

- User not found → log + skip (event) hoac `404` (API).
- Conflict state → log warning, idempotent no-op.
- DB error → retry event; API tra `500`.

## 11. Acceptance Criteria

- **AC1:** `USER_SUSPENDED` event → user khong login, sessions revoked.
- **AC2:** `USER_ENFORCEMENT_REVOKED` → user ACTIVE lai neu khong bi enforce khac.
- **AC3:** Idempotent replay khong gay double revoke loi.
- **AC4:** Sync suspend API va event apply cung ket qua.

## 12. Related

- `FR_SuspendUserByAdmin.md`
- `docs/feature_requirements/admin/FR_SuspendUser.md`, `FR_BanUser.md`, `FR_RestrictUser.md`, `FR_RevokeUserEnforcement.md`
- `docs/business_flow/admin_business_flow/cross-service-integration-flow.md`
- `docs/business-spec/admin-service-spec.md` (User Enforcement)
