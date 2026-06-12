# Handle User Banned Notification - Internal and Behavior

## 1. Business Goal

Notify **target user** when Admin publishes `USER_BANNED`: in-app + push per account-critical policy. Email handled by `AccountEnforcementNotificationEventHandler` (`FR_SendAccountEnforcementEmail`).

## 2. Trigger

- Kafka: `admin.user.banned`
- Internal ingest: `eventType` = `USER_BANNED`

## 3. Flow

1. **Ingest:** `AccountEnforcementEmailPayloadNormalizer` maps `user_id` to `target_user_id`, `description` to `enforcement_reason`, strips `enforced_by` and internal notes.
2. **Worker:** `UserSuspendedNotificationEventHandler` (`@Order(46)`) delivers in-app + push for `USER_SUSPENDED` and `USER_BANNED`.
3. **Email:** `AccountEnforcementNotificationEventHandler` (`@Order(47)`).
4. **Reference:** `USER_ENFORCEMENT/{enforcement_id}`.

## 4. Admin Payload (producer)

Required: `user_id`, `enforcement_id`. Optional: `reason_code`, `description`, `expires_at`, `action_type=BAN`.

## 5. Outcomes

| Outcome | Meaning |
|---------|---------|
| `COMPLETED` | In-app and/or push delivered |
| `FAILED` + `PERMANENT` | Missing `target_user_id` or `enforcement_id` |
| `FAILED` + `RETRYABLE` | DB or push provider error |
| `NO_OP` | User disabled in-app and has no device token |

## 6. Related FR

- `FR_HandleUserBannedNotification`, `FR_HandleUserSuspendedNotification`, `FR_SendAccountEnforcementEmail`.

## 7. FE / Client

- Deep link: `reference_type=USER_ENFORCEMENT`, `reference_id={enforcement_id}`.
- In-app title: **Account banned**.