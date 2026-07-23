# System Operations Admin UI — Checklist

## Routing & shell
- [x] `adminTabs.js` — section `systemOperations`
- [x] `adminUrlParams.js` — `configId`, `configView`, `announcementId`, `announcementView`, config/announcement filter params
- [x] `AdminNestedNav.jsx` — parent + child icons (`settings`, `announcement`, `model`)
- [x] `AdminPage.jsx` — tab map + URL handlers + list panels as siblings
- [x] Deep-link: `?section=systemOperations&tab=system-configs&configId={uuid}&configView=history`
- [x] Deep-link: `?section=systemOperations&tab=system-announcements&announcementId={uuid}`
- [x] Deep-link: `?section=systemOperations&tab=model-registry&mr_version={n}`

## Permissions
- [x] `SYSTEM_CONFIG_VIEW` / `SYSTEM_CONFIG_UPDATE`
- [x] `SYSTEM_ANNOUNCEMENT_*` (CREATE, UPDATE, PUBLISH, CANCEL)
- [x] MSW admin login includes permissions

## System configs tab
- [x] List + filters (`q`, `value_type`, `is_active`, pagination, page size)
- [x] Merged surface + stats bar + quick/active filter chips
- [x] Row-click drawer toggle + header CTA create
- [x] Create modal (POST)
- [x] Edit drawer (PATCH value/description + toggle)
- [x] History panel (`GET .../history`) with pagination + admin name resolve
- [x] Open drawer when `configId` not on current page (`GET .../{configId}`)
- [x] Field-level validation messages from API `errors[]`
- [x] Type-aware value input + secret mask UI
- [x] Audit deep-link from drawer

## System announcements tab
- [x] List + filters + stats bar (real counts via parallel list API)
- [x] Merged surface + quick/active filter chips + page size + numbered pagination
- [x] Row-click drawer + header CTA create + empty state CTA
- [x] Create draft drawer
- [x] Edit draft in drawer (`PATCH .../{id}`)
- [x] Publish wizard with audience picker (`recipient_user_ids`, `target_audience`)
- [x] Pin / cancel with confirm dialog in drawer actions tab
- [x] Banner preview component
- [x] `GET .../{announcementId}` deep-link + audit navigation
- [x] Field-level `errors[]` on create/edit
- [x] Dismiss **not** exposed in admin UI

## Model registry tab
- [x] `ModelRegistryListPanel` + merged surface + eyebrow header
- [x] Runtime status card (`GET /recommendation-model-status`)
- [x] Stats bar + quick/active filter chips (`mr_status`)
- [x] Row-click drawer with detail + structured metrics
- [x] Empty state with pipeline guidance (read-only, no export CTA)
- [x] URL deep-link `mr_version` + `mr_view`
- [x] MSW mocks for artifacts + status
- [x] Nav icon `model`
- [x] `modelName` in artifact API response
- [x] Unit tests for badge/status helpers

## API layer
- [x] `systemConfigApi.js` / `systemAnnouncementApi.js` via `adminApiClient`
- [x] `recommendationModelArtifactsApi.js` / `recommendationModelStatusApi.js` via `socialApiClient`
- [x] Snake_case mappers + detail fetch hooks

## MSW
- [x] `adminSystemOperationsHandlers.js` (configs + announcements GET/PATCH detail)
- [x] `adminModelRegistryHandlers.js` (artifacts + runtime status)
- [x] Registered in `mocks/handlers/index.js`

## QA
- [ ] Manual test with real admin-service
- [ ] Permission denied flows (403)
- [ ] Session expired (401)
- [x] `npm run build` passes

## Stitch alignment
- [ ] Visual pass vs `frontend/stitch/SYSTEM CONFIG/`
- [ ] Visual pass vs `frontend/stitch/SYSTEM ANNOUNCEMENTS/`
