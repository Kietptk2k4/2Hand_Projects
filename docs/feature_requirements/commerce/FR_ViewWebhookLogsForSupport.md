# Functional Requirement - View Webhook Logs For Support (Commerce)

## 1. Feature Overview

Commerce Service expose danh sach webhook logs (PayOS/GHN) co filter va pagination cho support troubleshooting.

## 2. Actors

- **Support Admin:** Tra cuu webhook logs (qua Admin Service).
- **Commerce Service:** Own webhook log storage.

## 3. Scope

**In Scope:**

- Paginated search by provider, reference, status, date range.
- Safe payload summary per log entry.

**Out of Scope:**

- Replay webhook.
- Edit/delete logs from API.

## 4. API Contract

**Endpoint:** `GET /commerce/api/v1/admin/support/webhook-logs`

**Auth:** Required — `WEBHOOK_SUPPORT_READ`

## 5. Business Rules

- Read-only.
- Pagination defaults and max size enforced.
- Raw payload never returned.

## 6. Database Impact

- Read webhook log tables (PayOS/GHN).

## 7. Transaction

- Read-only.

## 8. Security

- Permission required.
- Sanitized `payload_summary` only.

## 9. Failure Cases

- Invalid filter/pagination -> 400.
- Missing permission -> 403.

## 10. Acceptance Criteria

- Support can search webhook logs with permission.
- No secrets or full payloads exposed.

## 11. Related

- API: `docs/api_fe_behavior/commerce_api_fe_behavior/ViewWebhookLogsForSupport-api-and-behavior.md`
- Admin FR: `docs/feature_requirements/admin/FR_ViewWebhookLogsForSupport.md`