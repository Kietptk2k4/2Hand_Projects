# Functional Requirement - Log Admin Action

## 1. Feature Overview

Ghi lai moi hanh dong quan tri quan trong vao `admin_action_logs` de audit, investigation va compliance.

## 2. Actors

- **Admin Service:** Writes audit log.
- **Admin/Auditor:** Reviews logs.

## 3. Scope

**In Scope:**

- Log actor, action, target, result, IP and user agent.
- Support success/failure statuses.

**Out of Scope:**

- External SIEM streaming.
- Long-term archive.

## 4. API Contract

Internal application concern. No public endpoint required.

## 5. Business Rules

- Critical admin operations must write log in same transaction when possible.
- Failed permission/security attempts should be logged when useful and safe.
- Do not log tokens, passwords, OTPs or secrets.
- Logs are append-only.

## 6. Database Impact

- Insert `admin_action_logs`.

## 7. Transaction

- For domain-changing actions, log should be written in same transaction.
- For rejected requests, log may be written independently.

## 8. Security

- Sanitize request payload.
- Only authorized auditors can read logs.

## 9. Failure Cases

- Audit log write failure during critical write should fail the operation unless policy says fire-and-forget.

## 10. Acceptance Criteria

- Every critical admin action is traceable.
- Log contains actor, target, action, status and timestamp.
- Sensitive data is not stored.

