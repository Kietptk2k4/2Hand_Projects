# Mobile Social Scope - 2Hands

Version: 1.0  
Owner: Mobile Team  
Purpose: Define what the native social module includes, excludes, and defers — so AI does not over-build or copy unfinished web UI.

---

## 1) Context

- **Web reference (complete):** `frontend/src/fe-module/features/social/` (~150 files, 6 pages)
- **API contracts (shared web + mobile):** `docs/api_fe_behavior/social_api_fe_behavior/` (32 files)
- **Visual reference:** `frontend/stitch/social_feed`, `post_detail`, `user_profile`, `create_post`, `saved_post`, `search_post`
- **Mobile stack:** Expo SDK 56, JavaScript, expo-router — see `mobile/docs/mobile-master-context.md`

Social on web is the **source of truth for business flow and API mapping**. Mobile reimplements UI with React Native primitives; it does not duplicate API behavior docs.

---

## 2) MVP (v1) — In Scope

### 2.1 Core navigation shell

| Item | Notes |
|------|-------|
| Authenticated tab shell | `(tabs)` with Feed as primary social entry |
| Auth gate | Unauthenticated users → `app/(auth)/login.jsx` |
| Deep link scheme | `twohands://` (see `app.json`) |

### 2.2 Screens and flows

| Flow | Mobile v1 | Web reference |
|------|-----------|---------------|
| Global + Following feed | Yes | `SocialFeedPage.jsx` |
| Post detail (read, like, save, comment) | Yes — full screen | `PostDetailModal.jsx` + `FR_ViewPostDetail.md` |
| Create post | Yes | `CreatePostModal` / `PostFormModal.jsx` |
| Edit post | Yes | `EditPostModal` |
| Delete post (own) | Yes | `usePostActions` |
| User profile (self + others) | Yes | `SocialProfilePage.jsx` |
| Follow / unfollow | Yes | `useFollowActions`, `followApi.js` |
| Followers / following list | Yes | `FollowListModal.jsx` |
| Post likers list | Yes | `LikesListModal.jsx` |
| Saved posts | Yes | `SocialSavedPostsPage.jsx` |
| Search posts | Yes | `SocialSearchPostsPage.jsx` |
| Hashtag posts | Yes | `SocialHashtagPostsPage.jsx` |
| Suggested users | Yes | `SocialSuggestedUsersPage.jsx` |
| Trending hashtags (feed widget) | Yes — inline section on feed, not desktop sidebar | `FeedRightSidebar.jsx` |
| Product tag in post → view product | Yes — navigate to commerce product screen (when commerce tab exists) | `PostProductTagsBlock`, `useViewCommerceProduct` |
| Comment media (image) | Yes | `CommentComposer`, `useCommentMediaUpload` |
| Reply to comment | Yes | `ReplyComment-api-and-behavior.md` |
| Like comment | Yes | `LikeComment-api-and-behavior.md` |
| Social write blocked (suspended user) | Yes — banner + error handling | `SocialWriteBlockedBanner`, `socialWriteErrors.js` |

### 2.3 API layer to port (from web)

Port into `mobile/src/features/social/api/` (mirror web names):

| API module | Behavior doc(s) |
|------------|-----------------|
| `feedApi.js` | ViewGlobalFeed, ViewFollowingFeed |
| `postApi.js` | FR_ViewPostDetail, DeletePost |
| `createPostApi.js` | CreatePost, UploadPostMedia |
| `editPostApi.js` | EditPost |
| `commentApi.js` | CommentPost, ListPostComments, ReplyComment, DeleteOwnComment |
| `likesApi.js` | LikeUnlikePost, ViewPostLikers, LikeComment, ViewCommentLikers |
| `savePostApi.js` / `savedPostsApi.js` | SaveUnsavePost, ViewSavedPosts |
| `profileApi.js` | ViewSocialProfile |
| `userPostsApi.js` | ViewUserPosts |
| `followApi.js` | FollowUser, UnfollowUser |
| `relationsApi.js` | ViewFollowersFollowingList |
| `discoveryApi.js` | ViewSuggestedUsers, ViewTrendingHashtags |
| `searchPostsApi.js` | SearchPost |
| `searchHashtagApi.js` | SearchHashtag |
| `postProductTagApi.js` | TagProductInPost |

Shared HTTP client: `mobile/src/services/http/socialApiClient.js` (mirror `frontend/src/fe-module/services/http/socialApiClient.js`).

### 2.4 Non-functional requirements (v1)

- Loading, error (+ retry), and empty states on every list screen
- Pagination via `FlatList` `onEndReached` (page size 20, same as web `FEED_PAGE_SIZE`)
- JWT via existing auth refresh flow; no tokens in logs
- Field mapping: support `snake_case` from backend per API behavior docs
- UI tokens from `mobile/src/shared/theme/colors.js` and `mobile/docs/mobile-design-system.md`

---

## 3) Out of Scope (v1)

Do **not** implement in mobile v1 unless explicitly requested:

| Item | Reason |
|------|--------|
| **Share post** | Web shows toast "Tính năng đang được phát triển" (`PostCard` share button) |
| **Crop / filter image** in create post | Web coming soon (`PostFormModal`) |
| **Add location** to post | Web coming soon (`PostFormModal`) |
| Desktop **left sidebar** as separate column | `FeedLeftSidebar` is `hidden` on mobile web; stats belong on Profile tab |
| Desktop **right sidebar** as sticky column | Replace with feed sections (trending, suggestions teaser) |
| Sidebar links: Mạng lưới, Sự kiện | Commented out on web |
| Admin moderation | Web-only admin console |
| Backend / Kafka / outbox workers | `PublishSocialEvents`, `RetryFailedOutboxEvents`, `HandlePostModeratedEvent` — backend only |
| `ConsumeAuthUserEvents` | Backend consumer, not a mobile screen |

### v1 UX for out-of-scope actions

If a ported component still exposes a share/crop/location control from web copy-paste, either:

1. Hide the control, or  
2. Show the same message: **"Tính năng đang được phát triển."**

Do not invent new share sheets or location pickers.

---

## 4) Phase 2 (defer)

| Feature | Notes |
|---------|-------|
| Push notifications for social events | Depends on notification module + FCM |
| Offline draft posts | Not on web MVP |
| Native share sheet | After product defines share payload |
| Image crop / filters | After web ships |
| Location on posts | After web ships |
| Video upload optimizations | Use expo-av / expo-video; polish after images work |
| Tablet two-column feed | Optional layout breakpoint |

---

## 5) Dependencies on other mobile modules

| Module | Dependency |
|--------|------------|
| **Auth** | Login, session, `useCurrentUserId`, account profile for self profile header |
| **Commerce** | Product detail navigation from tagged products in posts (can stub route until commerce screens exist) |
| **Notification** | Optional badge on tab; not required for social v1 |

**Prerequisite before social feed:** working login + token storage (`src/features/auth/`, `src/services/auth/tokenStorage.js`).

---

## 6) Environment

```env
EXPO_PUBLIC_SOCIAL_SERVICE_BASE_URL=http://10.0.2.2:3002   # Android emulator
```

Local dev requires **social-service** on port **3002** and **auth-service** on **3001**.

---

## 7) Definition of Done (social feature)

- [ ] Screen listed in `mobile/docs/mobile-social-ui-map.md` exists under `app/`
- [ ] Business logic in `src/features/social/` (api + hooks + components)
- [ ] Matches relevant `docs/api_fe_behavior/social_api_fe_behavior/*` contract
- [ ] Loading / error / empty / pagination states
- [ ] No axios calls in `app/*.jsx`
- [ ] UTF-8 encoding on all new files (Windows)

---

## 8) Related documents

| Document | Role |
|----------|------|
| `mobile/docs/mobile-social-ui-map.md` | Web page/modal → mobile route mapping |
| `mobile/docs/mobile-social-implementation-order.md` | Build sequence and file checklist |
| `mobile/docs/mobile-convention.md` | Naming and folder rules |
| `mobile/docs/mobile-api-integration.md` | Response unwrap, 401 refresh |
| `mobile/docs/mobile-design-system.md` | Colors, typography, spacing |
