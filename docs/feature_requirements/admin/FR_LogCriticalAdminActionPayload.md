# Functional Requirement - Log Critical Admin Action Payload

## 1. Feature Overview

Luu payload da sanitize cho cac hanh dong admin critical de co the audit day du quyet dinh va du lieu thay doi.

## 2. Actors

- **Admin Service:** Sanitizes and logs payload.
- **Auditor/Super Admin:** Reads payload when investigating.

## 3. Scope

**In Scope:**

- Store sanitized `request_payload`.
- Store before/after summary when applicable.
- Mask sensitive fields.

**Out of Scope:**

- Raw request body storage.
- Password/token/secret logging.

## 4. API Contract

Internal audit concern.

## 5. Business Rules

- Payload logging applies only to critical actions.
- Sensitive fields must be removed or masked before persist.
- Payload should be compact and explainable, not full unrelated object graphs.

## 6. Database Impact

- Insert/update `admin_action_logs.request_payload`.

## 7. Transaction

- Same transaction as action log.

## 8. Security

- Strict redaction list: password, token, otp, secret, authorization, cookie.
- Access to payload requires audit permission.

## 9. Failure Cases

- Payload too large -> truncate or reject by policy.
- Serialization failure -> log metadata and fail critical action if audit completeness is required.

## 10. Acceptance Criteria

- Critical action payloads are audit-readable.
- Secrets are never stored.
- Payload is tied to action log entry.

