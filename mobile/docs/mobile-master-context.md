# Mobile Master Context - 2Hands

Version: 1.0  
Owner: Mobile Team  
Purpose: Single source of truth for AI implementing the native Expo app.

---

## 1) Fixed Tech Stack (required)

- Expo SDK 56
- React Native
- JavaScript only (no TypeScript)
- expo-router (file-based routing)
- axios for HTTP
- expo-secure-store for JWT refresh token storage

Implementation rules:

- Screen files use `.jsx` under `app/`
- Shared logic under `src/features/` and `src/services/`
- No API calls directly inside JSX event handlers beyond calling hooks/api functions
- Read Expo v56 docs: https://docs.expo.dev/versions/v56.0.0/

---

## 2) Priority Documents (read before coding)

### 2.1 Mobile engineering rules (this folder)

- `mobile/docs/mobile-master-context.md` (this file)
- `mobile/docs/mobile-convention.md`
- `mobile/docs/mobile-api-integration.md`
- `mobile/docs/mobile-design-system.md`

### 2.2 Repo-wide references

- `docs/engineering_rules/api-standard.md` — response envelope
- `docs/api_fe_behavior/*` — per-endpoint contract (same as web)
- `frontend/src/fe-module/` — port business flow and API mapping from web

### 2.3 Backend services (no changes from mobile)

| Service | Port (local) | API prefix |
|---------|--------------|------------|
| auth-service | 3001 | `/api/v1/auth`, `/api/v1/users` |
| social-service | 3002 | `/api/v1/social/*` |
| commerce-service | 3003 | `/commerce/api/v1/*` |
| notification-service | 3005 | `/api/v1/notification/*` |

Admin console is web-only for MVP.

---

## 3) Folder Structure

```text
mobile/
├── app/                    # expo-router screens
├── src/
│   ├── features/{domain}/  # api, hooks, components
│   ├── services/http/      # axios clients, refresh, unwrap
│   ├── services/auth/      # secure token storage
│   └── shared/theme/       # colors, spacing tokens
└── docs/                   # mobile conventions for AI
```

---

## 4) Definition of Done (mobile feature)

- [ ] Screen in `app/` with loading / error / empty states
- [ ] API via `src/features/*/api/` + `apiResponse.js` unwrap
- [ ] Matches `docs/api_fe_behavior` contract
- [ ] UI uses `src/shared/theme/colors.js` and design system doc
- [ ] No secrets logged; tokens in SecureStore only
- [ ] List files created/updated in PR or task summary

---

## 5) Prompt Template for AI

```text
Implement [SCREEN/FLOW] in mobile/ using Expo 56 + JavaScript + expo-router.

Read first:
- mobile/docs/mobile-master-context.md
- mobile/docs/mobile-convention.md
- mobile/docs/mobile-api-integration.md
- mobile/docs/mobile-design-system.md
- docs/api_fe_behavior/[relevant]-api-and-behavior.md

Port business logic from frontend/src/fe-module/features/[domain]/ when applicable.
Do not modify backend services unless explicitly requested.
```

---

## 6) Conflict Resolution

1. `mobile/docs/mobile-master-context.md`
2. `mobile/docs/mobile-api-integration.md`
3. `docs/api_fe_behavior/*`
4. `frontend/src/fe-module/` (reference only)
