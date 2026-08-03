## Context

Web FE already polls `GET …/notifications/unread-count` from `NotificationBadgeContext` and, when unread count increases after baseline, shows `FeedToast` and updates `unreadCount` for `NotificationBell` / `NotificationBadgePill`. Interval is 60s today with no Page Visibility handling.

Product acceptance is now explicit: **polling is fine**, but **badge must jump** and **toast must show** when unread increases. Reports that neither happens mean this change is not “interval only” — implementation must prove the FE loop updates UI, and verify must separate FE bugs from empty `user_notifications` / disabled workers.

## Goals / Non-Goals

**Goals:**

- Visible-tab poll every **15 seconds**.
- Pause interval while `document.hidden`; on visible again → **refetch immediately** then restart interval.
- **Definition of Done:** when a poll or resume refetch returns `count` greater than the session tracked previous count:
  1. badge unread count in the UI updates to the new value, and
  2. new-notification toast is shown (existing message rules).
- Preserve baseline seed on first load (no toast for pre-existing unread alone).
- Do not reset baseline / seen-ids solely on visibility change.
- Fix FE defects that block (1) or (2) when the API already returns an increased count.

**Non-Goals:**

- WebSocket / SSE / STOMP realtime.
- Env-based demo vs prod interval split.
- Mobile AppState / FCM changes.
- Redesigning notification ingest, Kafka topology, or Redis for realtime.
- GHN webhook registration / carrier callback demo.

## Decisions

### 1. Single fixed interval of 15s

- **Choice:** `NOTIFICATION_POLL_INTERVAL_MS = 15_000`.
- **Why:** Faster feedback without 5s load; one constant for all envs.
- **Alternatives:** 60s (too slow); Vite env override (deferred).

### 2. Page Visibility API only

- **Choice:** `visibilitychange` + `document.hidden` / `visibilityState`.
- **Why:** Correct “tab shown” signal; quieter than window focus.
- **Alternatives:** focus/blur; rely on browser throttle alone.

### 3. Resume = refetch then interval

- **Choice:** visible → `refetch()` then `setInterval(..., 15_000)`.
- **Why:** Badge/toast without waiting another full interval after Alt-Tab.
- **Alternatives:** interval-only resume; reset tracking on resume (reject).

### 4. Acceptance is UI outcomes, not “preserve code path”

- **Choice:** Treat badge update + toast as hard requirements when unread delta is detected.
- **Why:** Current product feedback is that toast and count do not appear; tuning interval alone is insufficient as DoD.
- **Alternatives:** Assume existing behavior works (rejected).

### 5. Diagnose split: FE vs empty pipeline

- **Choice:** Verify with DevTools/`unread-count`:
  - If `data.count` increases but UI does not → **FE fix in this change**.
  - If `data.count` stays 0 with no new list items → note local pipeline (`NOTIFICATION_PROCESS_EVENTS_ENABLED`, Kafka/ingest); FE change still ships poll/visibility; pipeline fix is a separate follow-up unless a tiny FE error-handling gap hides failures.
- **Why:** Avoid boiling the ocean while still not declaring DoD met on a broken empty DB.

### 6. Keep logic in `NotificationBadgeContext`

- **Choice:** Extend poll effect there; optional small helper in the same feature folder.
- **Why:** Toast already mounts on the provider; badge reads `unreadCount` from context.

### 7. Cleanup

- **Choice:** Clear interval + remove visibility listener on cleanup; no poll when logged out.

## Risks / Trade-offs

- **[Risk] Higher API volume while visible** → 15s + pause when hidden.
- **[Risk] Rapid visibility toggles / overlapping refetch** → clear/restart carefully; keep existing error handling; prefer no double toast for same delta.
- **[Risk] DoD blocked by backend not creating notifications** → Verify checklist separates FE vs pipeline; do not fake toast without real count increase.
- **[Risk] Baseline seed hides “already unread on login” toast** → By design; DoD is for **new** increases after baseline.
- **[Trade-off] Not true realtime** → Accepted; no WebSocket in this change.

## Migration Plan

- FE-only deploy for poll/visibility (+ any FE badge/toast fixes).
- Rollback: restore `60_000` and previous always-on interval effect.

## Open Questions

- None blocking. Optional later: `VITE_NOTIFICATION_POLL_MS`.
