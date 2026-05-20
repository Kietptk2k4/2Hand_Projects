# Functional Requirement - Retry Failed Email Notification

## 1. Feature Overview

Retry failed email deliveries caused by transient provider/network errors.

## 2. Actors

- **Delivery Retry Worker:** Retries email.
- **Email Provider:** Receives retry.
- **User:** Receives email if retry succeeds.

## 3. Scope

**In Scope:**

- Select retryable failed email deliveries.
- Apply backoff and max retry.
- Re-render template safely.
- Retry provider send.

**Out of Scope:**

- Retrying invalid recipient forever.
- Manual provider dashboard operation.
- Per-channel delivery table in MVP.

## 4. Business Rules

- Retry transient errors: timeout, rate limit, provider 5xx.
- Do not retry permanent errors: invalid email, missing template, malformed payload.
- Reset/verification token/link must still be valid; if expired, stop retry or require new Auth event.
- Do not log tokens/links.
- Latest settings are respected unless critical override applies.

## 5. Database Impact

- Read `notification_events`/`user_notifications` according to implementation.
- Update delivery status/retry metadata.

## 6. Transaction

- Select retry candidates in small batches.
- Do not hold DB transaction during provider call.

## 7. Failure Cases

- Provider still unavailable -> keep failed and retry later.
- Token expired -> permanent failure/needs new event.
- Max retries exceeded -> stop retry.

## 8. Acceptance Criteria

- Retryable email failures are retried.
- Permanent email failures are not retried forever.
- Sensitive token/link remains protected.

