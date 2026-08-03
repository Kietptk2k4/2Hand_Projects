## Why

In-app notification feedback on web is too slow (60s poll) and currently fails the product bar: when a new notification exists, users do not reliably see the bell **badge count jump** or a **toast**. We keep polling (no WebSocket) but must make badge + toast work within the poll/visibility loop so local demos and normal browsing feel responsive.

## What Changes

- Lower web notification unread-count poll interval from 60s to **15s** (single fixed constant).
- Pause polling when the document is hidden (`visibilitychange` / `document.hidden`).
- On tab become visible again: **refetch immediately**, then restart the interval.
- **Acceptance (required):** after a successful poll/resume where unread count increases vs the session’s tracked count:
  - the bell **badge count MUST update** (jump), and
  - a **new-notification `FeedToast` MUST appear**.
- Fix any FE bugs in `NotificationBadgeContext` / badge wiring that prevent toast or badge update when `unread-count` already returns a higher count.
- During verify: if Network shows `count` stuck at 0 while events should create in-app rows, document the backend pipeline gap (ingest/worker) — out of scope to redesign Kafka, but in-scope to confirm FE is not the sole failure.
- No WebSocket / SSE / STOMP.
- No mobile AppState polling in this change.

## Capabilities

### New Capabilities

- `notification-badge-polling`: Web client unread badge refresh and new-notification toast via polling, including 15s interval, Page Visibility pause/resume, and mandatory badge + toast on unread increase.

### Modified Capabilities

- (none)

## Impact

- **Frontend:** `NotificationBadgeContext`, `notificationConstants`, `NotificationBell` / `NotificationBadgePill` if wiring fixes needed.
- **Backend / notification-service:** no API contract change; verify-only if count stays 0 (workers/Kafka/local ingest).
- **Mobile:** none.
- **Gateway / GHN webhook:** none.
- **Load:** more frequent `GET …/unread-count` while tab visible; fewer while hidden.
