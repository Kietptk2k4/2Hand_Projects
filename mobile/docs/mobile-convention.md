# Mobile Convention - 2Hands

Version: 1.0  
Scope: `mobile/` Expo app.

---

## 1. Goals

- Align with backend API contracts shared with web
- Feature-first structure mirroring `frontend/src/fe-module/features/`
- Thin screens, fat hooks/api layer

---

## 2. Directory Layout

```text
mobile/
├── app/
│   ├── _layout.jsx           # root Stack / Tabs
│   ├── index.jsx             # entry / auth gate
│   ├── (auth)/               # unauthenticated routes
│   └── (tabs)/               # authenticated shell (future)
├── src/
│   ├── features/
│   │   └── auth/
│   │       ├── api/authApi.js
│   │       └── hooks/        # useLogin, useSession (future)
│   ├── services/
│   │   ├── http/
│   │   │   ├── apiResponse.js
│   │   │   ├── authApiClient.js
│   │   │   ├── authRefreshService.js
│   │   │   └── resolveServiceBaseUrl.js
│   │   └── auth/tokenStorage.js
│   └── shared/
│       ├── theme/colors.js
│       └── constants/routes.js  # future
```

---

## 3. Naming

| Kind | Convention | Example |
|------|------------|---------|
| Screen file | `kebab-case.jsx` in `app/` | `app/(auth)/login.jsx` |
| Component | `PascalCase.jsx` | `ProductCard.jsx` |
| Hook | `useCamelCase.js` | `useCart.js` |
| API module | `camelCase.js` | `authApi.js` |
| Constant | `UPPER_SNAKE_CASE` | `API_TIMEOUT_MS` |

---

## 4. Rules

**Allowed**

- Screen composes hooks + presentational components
- `src/features/*/api/*` calls axios clients
- Map `snake_case` backend fields in api layer or dedicated `*Mapper.js`

**Forbidden**

- axios/fetch directly in `app/*.jsx` (use feature api module)
- Storing refresh token in AsyncStorage without encryption — use `expo-secure-store`
- Duplicating backend business rules (validate on server; client validation is UX only)
- TypeScript files unless project explicitly migrates

---

## 5. Routing (expo-router)

- Unauthenticated: `app/(auth)/login.jsx`
- Post-login shell: `app/(tabs)/` (to be added)
- Deep links scheme: `twohands://` (see `app.json`)

Navigation:

```javascript
import { router } from "expo-router";
router.replace("/");
router.push("/commerce/product/123");
```

---

## 6. Porting from Web

When adding a feature already on web:

1. Read `frontend/src/fe-module/features/{domain}/api/*.js`
2. Read matching `docs/api_fe_behavior/*`
3. Copy unwrap/error pattern from `src/services/http/apiResponse.js`
4. Rebuild UI with React Native primitives (`View`, `Text`, `Pressable`, `FlatList`)
5. Do not copy Tailwind classNames — use `StyleSheet` or shared theme tokens

---

## 7. File Encoding

All `*.js`, `*.jsx`, `*.md` must be **UTF-8 without BOM** (Windows).  
See `docs/engineering_rules/file-encoding-standards.md`.
