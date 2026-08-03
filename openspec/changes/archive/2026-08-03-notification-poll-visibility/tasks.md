## 1. Constants

- [x] 1.1 Update `NOTIFICATION_POLL_INTERVAL_MS` from `60_000` to `15_000` in `frontend/src/fe-module/features/notification/constants/notificationConstants.js`

## 2. Visibility-aware polling

- [x] 2.1 In `NotificationBadgeContext`, replace the always-on `setInterval` effect with visibility-aware logic: start interval only when authenticated and `document.visibilityState === "visible"`
- [x] 2.2 On `visibilitychange` to hidden: clear the poll interval (do not reset baseline / seen notification ids)
- [x] 2.3 On `visibilitychange` to visible (while authenticated): call `refetch()` immediately, then start the 15s interval
- [x] 2.4 Ensure effect cleanup clears the interval and removes the `visibilitychange` listener; keep unauthenticated path with no polling

## 3. Badge + toast correctness (FE)

- [x] 3.1 Confirm `refetch` updates context `unreadCount` whenever `unread-count` returns a new value after baseline (badge reads this via `NotificationBell` / `NotificationBadgePill`)
- [x] 3.2 Confirm unread increase path still calls toast (`showToastForNewNotifications` / `FeedToast`) and does not clear toast incorrectly on visibility resume
- [x] 3.3 Fix any FE wiring/state bugs found where API `count` increases but badge and/or toast do not update

## 4. Verify (Definition of Done)

- [ ] 4.1 Manually verify: visible tab polls ~every 15s; hide tab → no periodic unread-count; show tab → immediate unread-count then interval resumes
- [ ] 4.2 Manually verify **DoD**: after baseline, when unread increases (real backend or controlled test data), **badge count jumps** and **new-notification toast appears** within one poll/resume cycle
- [ ] 4.3 If `unread-count` stays `0` / no new list items while a domain event was expected: note pipeline gap (process worker / Kafka / ingest) in the PR or task notes — do not mark DoD satisfied by FE-only timing changes
