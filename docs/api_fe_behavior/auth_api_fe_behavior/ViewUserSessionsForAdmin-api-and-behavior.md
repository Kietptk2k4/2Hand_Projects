# View User Sessions For Admin - API and Behavior Spec

## 1. Scope
This document defines the Auth Service API for admins/support to list refresh-token sessions of a target user during account investigation.

In scope:
- Paginated session list with optional status filter.
- Session metadata without token/hash leakage.

Out of scope:
- Revoking sessions (separate admin revoke APIs).
- Binary upload or login log history.

## 2. Source Docs
- `docs/feature_requirements/auth/FR_ViewUserSessionsForAdmin.md`
- `docs/api_fe_behavior/auth_api_fe_behavior/update-avatar-api-and-behavior.md` (user self-service sessions reference)

## 3. API Contract

### 3.1 Endpoint
- Method: `GET`
- Path: `/api/v1/admin/users/{userId}/sessions`
- Auth: Bearer JWT with `USER_INVESTIGATION_READ`

### 3.2 Query Parameters
| Param | Default | Rules |
|-------|---------|-------|
| `status` | `ACTIVE` | `ACTIVE`, `LOGGED_OUT`, `REVOKED`, `EXPIRED`, `ALL` |
| `page` | `1` | `>= 1` |
| `limit` | `20` | `1`–`50` |

### 3.3 Success Response (200)
```json
{
  "code": 200,
  "success": true,
  "message": "Lay danh sach phien dang nhap thanh cong.",
  "data": {
    "user_id": "uuid",
    "sessions": [
      {
        "session_id": "uuid",
        "device_id": "device-abc",
        "ip_address": "203.0.113.1",
        "user_agent": "Chrome/120 ...",
        "status": "ACTIVE",
        "created_at": "2026-05-19T12:00:00Z",
        "updated_at": "2026-05-19T12:00:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total_items": 2,
      "has_next": false
    }
  },
  "errors": null,
  "timestamp": "2026-05-21T10:00:00Z"
}
```

## 4. Error Handling
- `400`: invalid `status`, `page`, or `limit`.
- `401`: missing/invalid JWT.
- `403`: actor lacks `USER_INVESTIGATION_READ`.
- `404`: target user not found or deleted.

## 5. Backend Behavior
1. Resolve actor from JWT.
2. Check `USER_INVESTIGATION_READ`.
3. Load target user; reject deleted users as `404`.
4. Query `refresh_token_sessions` ordered by `created_at DESC`.
5. Map rows without `token_hash` / refresh token fields.

## 6. FE Behavior
- Use in user investigation "Active sessions" tab.
- Default filter `ACTIVE`; offer `ALL` for short history review.
- Use `pagination.has_next` for load more.
- After revoke flows (if available), refetch this list.

## 7. Acceptance Criteria
- Authorized admin sees ACTIVE sessions for existing user.
- Response never includes token/hash fields.
- Missing user returns `404`.
