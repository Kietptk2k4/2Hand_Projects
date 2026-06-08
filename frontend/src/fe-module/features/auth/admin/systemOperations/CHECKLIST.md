# System Operations Admin UI — Checklist

## Routing & shell
- [x] `adminTabs.js` — section `systemOperations`
- [x] `adminUrlParams.js` — `configId`, `configView`, config/announcement filter params
- [x] `AdminNestedNav.jsx` — parent + child icons (`settings`, `announcement`)
- [x] `AdminPage.jsx` — tab map + URL handlers
- [ ] Deep-link: `?section=systemOperations&tab=system-configs&configId={uuid}&configView=history`

## Permissions
- [x] `SYSTEM_CONFIG_VIEW` / `SYSTEM_CONFIG_UPDATE`
- [x] `SYSTEM_ANNOUNCEMENT_*` (CREATE, UPDATE, PUBLISH, CANCEL)
- [x] MSW admin login includes permissions

## System configs tab
- [x] List + filters (`q`, `value_type`, `is_active`, pagination)
- [x] Create modal (POST)
- [x] Edit drawer (PATCH value/description + toggle)
- [x] History panel (`GET .../history`)
- [ ] Open drawer when `configId` not on current page (fetch-by-id — backend TBD)
- [ ] Field-level validation messages from API `errors[]`
- [ ] JSON editor / type-aware value input

## System announcements tab
- [x] List + filters + stats cards (page slice)
- [x] Create draft drawer
- [x] Publish / pin / cancel with confirm dialog
- [x] Dismiss **not** exposed in admin UI
- [ ] Edit draft content (PATCH — if API added later)
- [ ] Publish audience / recipient picker (`recipient_user_ids`, `target_audience`)

## API layer
- [x] `systemConfigApi.js` / `systemAnnouncementApi.js` via `adminApiClient`
- [x] Snake_case mappers

## MSW
- [x] `adminSystemOperationsHandlers.js`
- [x] Registered in `mocks/handlers/index.js`

## QA
- [ ] Manual test with real admin-service
- [ ] Permission denied flows (403)
- [ ] Session expired (401)
- [x] `npm run build` passes

## Stitch alignment
- [ ] Visual pass vs `frontend/stitch/SYSTEM CONFIG/`
- [ ] Visual pass vs `frontend/stitch/SYSTEM ANNOUNCEMENTS/`