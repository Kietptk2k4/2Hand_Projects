# View Login History For Admin - API and Behavior Spec

## 1. Scope
This document defines the Auth Service API for admins/support to read login attempt history of a target user during investigation.

In scope:
- Paginated login logs with optional filters (`success`, `from`, `to`).
- Metadata only; no secrets in response.

Out of scope:
- Mutating login logs.
- Session revoke (separate APIs).

## 2. Source Docs
- `docs/feature_requirements/auth/FR_ViewLoginHistoryForAdmin.md`
- `docs/api_fe_behavior/auth_api_fe_behavior/ViewUserSessionsForAdmin-api-and-behavior.md`

## 3. API Contract

### 3.1 Endpoint
- Method: `GET`
- Path: `/api/v1/admin/users/{userId}/login-history`
- Auth: Bearer JWT with `USER_INVESTIGATION_READ`

### 3.2 Query Parameters
| Param | Default | Rules |
|-------|---------|-------|
| `page` | `1` | `>= 1` |
| `limit` | `20` | `1`–`100` |
| `success` | — | optional `true` / `false` |
| `from` | — | optional ISO-8601 instant |
| `to` | — | optional ISO-8601 instant; must be `>= from` when both set |

### 3.3 Success Response (200)
```json
{
  "code": 200,
  "success": true,
  "message": "Lay lich su dang nhap thanh cong.",
  "data": {
    "user_id": "uuid",
    "items": [
      {
        "login_method": "EMAIL",
        "ip_address": "203.0.113.1",
        "user_agent": "Mozilla/5.0 ...",
        "success": true,
        "created_at": "2026-05-20T08:00:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total_items": 42,
      "total_pages": 3,
      "has_next": true
    }
  },
  "errors": null,
  "timestamp": "2026-05-21T10:00:00Z"
}
```

## 4. Error Handling
- `400`: invalid pagination or filter values.
- `401`: missing/invalid JWT.
- `403`: missing `USER_INVESTIGATION_READ`.
- `404`: target user not found or deleted.

## 5. Backend Behavior
1. Resolve actor from JWT and check investigation permission.
2. Load target user; deleted users return `404`.
3. Query `login_logs` ordered by `created_at DESC`.
4. Map items without internal log id or tokens.
5. Truncate `user_agent` when longer than 512 characters.

## 6. FE Behavior
- Tab "Login history" in user investigation UI.
- Show success/failure badge per row.
- Use `pagination.has_next` or `total_pages` for paging / infinite scroll.

## 7. Acceptance Criteria
- Authorized admin can list login history for an existing user.
- Response contains no secrets or internal ids.
- Missing user returns `404`.
- Pagination totals are consistent with filters.
