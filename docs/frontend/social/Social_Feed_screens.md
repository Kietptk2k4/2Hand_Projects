# Social Feed Screens Spec - 2Hands Frontend

Version: 1.0
Scope: Social Home Feed (Global + Following tabs), protected route `/social`.

---

## 1. Mục tiêu

Implementation-ready spec cho màn Social Home Feed, tổng hợp từ:

- `docs/engineering_rules/fe-master-context.md`
- `docs/engineering_rules/frontend-api-integration.md`
- `docs/engineering_rules/frontend-convention.md`
- `docs/engineering_rules/design-system.md`
- `docs/api_fe_behavior/social_api_fe_behavior/ViewGlobalFeed-api-and-behavior.md`
- `docs/api_fe_behavior/social_api_fe_behavior/ViewFollowingFeed-api-and-behavior.md`
- `frontend/stitch/social_feed/DESIGN.md`, `code.html`, `screen.png`

---

## 2. Route map

| Route | Guard | Mô tả |
|-------|-------|-------|
| `/social` | AuthGuard | Social Home Feed |
| `/social/posts/:postId` | AuthGuard | Placeholder chi tiết bài viết |

---

## 3. API integration

| Tab | Endpoint | Query |
|-----|----------|-------|
| Toàn cầu | `GET /api/v1/social/feed/global` | `page` (default 0), `size` (default 20, max 50) |
| Đang theo dõi | `GET /api/v1/social/feed/following` |同上 |

- Base URL: `VITE_SOCIAL_SERVICE_BASE_URL`
- Client: `socialApiClient` (JWT + refresh 401 giống auth)
- API layer: `features/social/api/feedApi.js` unwrap envelope trước khi trả UI
- JSON field: **camelCase** (`postId`, `likeCount`, …)

Query keys (chuẩn bị invalidate):

- `feedKeys.global(page, size)`
- `feedKeys.following(page, size)`

---

## 4. UI layout (Stitch)

- Header: tái sử dụng `AppHeader`
- Desktop: 3 cột (profile/quick links | feed | trending/suggestions)
- Mobile: ẩn sidebars, feed full width
- Tab: **Toàn cầu** | **Đang theo dõi**
- Composer: placeholder UI (chưa gọi Create Post API)
- PostCard: caption (collapse + Xem thêm), media grid, hashtags, counters, actions (coming soon)

---

## 5. States

| State | Hành vi |
|-------|---------|
| Loading | 2 skeleton cards lần đầu |
| Empty | HTTP 200, `items: []` — copy theo tab |
| Error | Banner + nút **Thử lại** |
| Load more | Nút **Tải thêm** khi `meta.hasNext === true` (append page) |

Đổi tab → reset `page` về 0, fetch lại feed tương ứng.

---

## 6. File map

```txt
frontend/src/fe-module/
├── services/http/socialApiClient.js
├── features/social/
│   ├── api/feedApi.js, feedKeys.js
│   ├── hooks/useFeed.js
│   ├── pages/SocialFeedPage.jsx, PostDetailPlaceholderPage.jsx
│   ├── components/ (FeedTabs, FeedComposer, PostCard, sidebars, …)
│   ├── constants/feedTabs.js
│   └── utils/ (formatRelativeTime, authorDisplay)
```

---

## 7. Env & MSW mock

```env
VITE_SOCIAL_SERVICE_BASE_URL=http://localhost:3002
VITE_USE_MOCK=true
```

Khi `VITE_USE_MOCK=true`, MSW intercept:

- `GET */api/v1/social/feed/global`
- `GET */api/v1/social/feed/following`

Mock data: `frontend/src/mocks/data/socialFeedData.js`  
Handlers: `frontend/src/mocks/handlers/socialFeedHandlers.js`

Đăng nhập mock: `active@2hands.vn` / `Password123!` → mở `/social`.

Post detail (modal): `/social?postId=674a10000000000000000001` (ObjectId **24** ký tự hex).

---

## 8. Assumptions

- Feed API chưa trả `author` object — UI hiển thị placeholder từ `authorId` (Stitch layout, API data).
- Like/comment/share/follow/composer: toast "Tính năng đang được phát triển."
- Chưa cài `@tanstack/react-query` — dùng hook `useFeed` (có thể migrate sau).
