# UC - User Enforcement

## 1. Overview

Use case nay mo ta nghiep vu admin suspend, ban, restrict va revoke enforcement tren user. Admin Service luu enforcement decision va logs; Auth/Social/Commerce apply effect qua API/event integration.

## 2. Actors

- **Admin/Moderator:** Tao/revoke enforcement.
- **System:** Expire temporary enforcement.
- **Auth/Social/Commerce:** Consume enforcement events and apply policy.

## 3. Related Data

- `user_enforcements`
- `user_enforcement_logs`
- `admin_action_logs`
- `outbox_events`

## 4. Business Rules

- Enforcement action types: `BAN`, `SUSPEND`, `RESTRICT`.
- Enforcement status: `ACTIVE`, `REVOKED`, `EXPIRED`.
- Reason code and description required.
- Every status transition writes `user_enforcement_logs`.
- Critical enforcement action writes `admin_action_logs`.
- Cross-service event is written through outbox.

## 5. Sub-Use Cases

### 5.1. Suspend User

**Main Flow:**

1. Admin selects user and reason.
2. System checks `USER_SUSPEND`.
3. System creates active enforcement `SUSPEND`.
4. System writes enforcement log and admin action log.
5. System publishes `USER_SUSPENDED`.

**Postconditions:** Auth should suspend user and revoke sessions.

### 5.2. Ban User

**Main Flow:** Same as suspend but action type `BAN` and event `USER_BANNED`.

**MVP Note:** Auth may apply same effect as suspended.

### 5.3. Restrict User

**Main Flow:**

1. Admin creates `RESTRICT` enforcement.
2. System publishes `USER_RESTRICTED`.
3. Social/Commerce block configured write actions while login remains allowed.

### 5.4. Revoke Enforcement

**Main Flow:**

1. Admin selects active enforcement.
2. System checks permission.
3. System sets status `REVOKED`.
4. System writes logs and publishes `USER_ENFORCEMENT_REVOKED`.

### 5.5. View Current Enforcement And History

**Main Flow:**

1. Admin requests user enforcement view.
2. System returns active enforcements and historical logs.

## 6. Acceptance Criteria

- Enforcement requires permission.
- Enforcement logs are always written.
- Active enforcement can be revoked.
- Events are published through outbox.

