# Consume Auth User Events - Behavior Spec (Social Service)

## 1. Scope
Background consumer that synchronizes MongoDB `user_projections` from Auth/Admin user lifecycle events.

In scope:
- Kafka consumer group `social-user-projection`.
- Idempotent processing via PostgreSQL `processed_domain_events`.
- Event types: `USER_CREATED`, `USER_UPDATED`, `USER_DELETED`, enforcement events.

Out of scope:
- Publishing source events (Auth/Admin).
- Storing email/phone/password in projection.

## 2. Source Docs
- `docs/feature_requirements/social/FR_ConsumeAuthUserEvents.md`
- `docs/business_flow/auth_business_flow/profile-privacy-flow.md`

## 3. Topics (default)
| Event type | Topic |
|------------|-------|
| USER_CREATED | `auth.user.created` |
| USER_UPDATED | `auth.user.updated` |
| USER_DELETED | `auth.user.deleted` |
| USER_SUSPENDED | `admin.user.suspended` |
| USER_BANNED | `admin.user.banned` |
| USER_RESTRICTED | `admin.user.restricted` |
| USER_ENFORCEMENT_REVOKED | `admin.user.enforcement_revoked` |
| USER_ENFORCEMENT_EXPIRED | `admin.user.enforcement_expired` |

## 4. Projection Mapping
| Event | `user_projections.status` |
|-------|---------------------------|
| USER_CREATED | payload `status` or `ACTIVE` |
| USER_UPDATED | update profile fields; keep status if omitted |
| USER_DELETED | `DELETED` |
| USER_SUSPENDED / USER_BANNED | `SUSPENDED` |
| USER_ENFORCEMENT_REVOKED / EXPIRED | `ACTIVE` |
| USER_RESTRICTED | unchanged (MVP) |

Fields updated when present: `display_name`, `avatar_url`, `is_private`.

## 5. Idempotency
- Primary key: `event_id` in `processed_domain_events`.
- Duplicate `event_id` → skip Mongo write.

## 6. Configuration
```yaml
social:
  kafka:
    consumer:
      enabled: true
      bootstrap-servers: localhost:9092
      group-id: social-user-projection
```

Set `SOCIAL_KAFKA_CONSUMER_ENABLED=true` in runtime environments with Kafka.

## 7. Failure Handling
- Invalid payload (missing `user_id` / `event_id`) → log error, acknowledge to avoid poison-loop (MVP).
- Transient errors → Kafka retry (3 attempts, 1s backoff).
- Mongo/DB errors → retry via consumer error handler.

## 8. Downstream Impact
- `CreatePostUseCase` and other write paths read projection to block `SUSPENDED` / `DELETED`.
- Public profile APIs use projection for display and privacy.

## 9. Acceptance Criteria
- USER_CREATED creates projection with profile fields when provided.
- USER_UPDATED merges avatar/privacy.
- USER_SUSPENDED sets SUSPENDED.
- USER_DELETED sets DELETED.
- Duplicate event_id does not corrupt data.
