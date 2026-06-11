# Mobile Social Implementation Order - 2Hands

Version: 1.0  
Owner: Mobile Team  
Purpose: Step-by-step build sequence, dependencies, and file checklist so AI implements social module in the correct order without breaking auth or duplicating work.

---

## 1) Prerequisites (must be done first)

| Step | Status | Files / notes |
|------|--------|---------------|
| Auth login works | Exists | `app/(auth)/login.jsx`, `src/features/auth/api/authApi.js` |
| Token storage | Exists | `src/services/auth/tokenStorage.js` |
| Auth HTTP client + refresh | Exists | `src/services/http/authApiClient.js`, `authRefreshService.js` |
| Env social URL | Configured | `EXPO_PUBLIC_SOCIAL_SERVICE_BASE_URL` in `.env` |
| social-service running | Dev | Port **3002** |
| Read social docs | Required | `mobile-social-scope.md`, `mobile-social-ui-map.md`, this file |

**Gate:** User can log in and reach a post-login screen before starting feed work.

---

## 2) Implementation phases

```text
Phase 0: HTTP + constants
    ↓
Phase 1: Tab shell + empty feed
    ↓
Phase 2: Feed list (read-only)
    ↓
Phase 3: Post detail + comments
    ↓
Phase 4: Post actions (like, save, delete)
    ↓
Phase 5: Create / edit post + media upload
    ↓
Phase 6: Profile + follow
    ↓
Phase 7: Secondary screens (saved, search, hashtag, suggestions)
    ↓
Phase 8: Polish (video, product tags → commerce, toasts)
```

Each phase should be mergeable and testable on emulator before the next.

---

## 3) Phase 0 — Social HTTP foundation

**Goal:** Call social API with same auth/unwrap pattern as web.

### Create

| File | Action |
|------|--------|
| `src/services/http/socialApiClient.js` | Create — copy pattern from `frontend/.../socialApiClient.js`; use `resolveServiceBaseUrl(process.env.EXPO_PUBLIC_SOCIAL_SERVICE_BASE_URL)` |
| `src/features/social/api/socialApiResponse.js` | Port from web |
| `src/features/social/constants/feedTabs.js` | Port from web |
| `src/shared/constants/routes.js` | Create — social path helpers |

### Port API modules (minimal for Phase 2)

| File | Priority |
|------|----------|
| `feedApi.js` | P0 |
| `postApi.js` | P0 |
| `feedKeys.js`, `postKeys.js` | P0 |

### Verify

- Manual or unit test: `GET /api/v1/social/feed/global?page=0&size=20` returns unwrapped `items` array with valid JWT.

---

## 4) Phase 1 — Authenticated tab shell

**Goal:** After login, user lands on tab navigator with placeholder feed.

### Create

| File | Action |
|------|--------|
| `app/(tabs)/_layout.jsx` | Bottom tabs: Feed, Shop (placeholder), Profile |
| `app/(tabs)/feed.jsx` | Placeholder "Feed" text |
| `app/(tabs)/profile.jsx` | Placeholder or redirect to self profile |
| `app/index.jsx` | Update — auth gate: token → `/(tabs)/feed`, else `/(auth)/login` |
| `app/_layout.jsx` | Update — register `(tabs)` stack |

### Verify

- Login → tabs visible → Feed tab selected.
- Logout (when implemented) → back to login.

---

## 5) Phase 2 — Feed (read-only)

**Goal:** Global + Following tabs, infinite scroll, skeleton / error / empty.

### Port hooks

| File | Source (web) |
|------|----------------|
| `hooks/useFeed.js` | `features/social/hooks/useFeed.js` |

### Create components

| File | Source (web) |
|------|----------------|
| `components/FeedTabs.jsx` | `FeedTabs.jsx` |
| `components/FeedPostSkeleton.jsx` | `FeedPostSkeleton.jsx` |
| `components/PostCard.jsx` | `PostCard.jsx` — read-only first (no like/save actions) |
| `components/PostMediaCarousel.jsx` | `PostMediaCarousel.jsx` |
| `components/PostCaption.jsx` | From `PostCard` dependencies |
| `utils/formatSocialCount.js` | Port |

### Update screen

| File | Work |
|------|------|
| `app/(tabs)/feed.jsx` | Compose FeedTabs + FlatList + states |

### API behavior docs

- `ViewGlobalFeed-api-and-behavior.md`
- `ViewFollowingFeed-api-and-behavior.md`

### Verify

- [ ] Initial skeleton
- [ ] Posts render with media + caption
- [ ] Empty messages per tab
- [ ] Error + "Thử lại"
- [ ] Load more when scrolling to end
- [ ] Tap post → navigate to detail (can be empty screen until Phase 3)

---

## 6) Phase 3 — Post detail + comments

**Goal:** Full post view with comment list and add comment.

### Create routes

| File |
|------|
| `app/post/[postId]/index.jsx` |

### Port

| Category | Files |
|----------|-------|
| API | `commentApi.js` |
| Hooks | `usePostComments.js`, `usePostDetail` logic from modal |
| Components | `PostDetailComments.jsx`, `CommentItem.jsx`, `CommentComposer.jsx`, `CommentMediaDisplay.jsx` |
| Utils | `mapCommentItem.js`, `commentConstants.js` |

### API behavior docs

- `FR_ViewPostDetail.md`
- `ListPostComments-api-and-behavior.md`
- `CommentPost-api-and-behavior.md`
- `ReplyComment-api-and-behavior.md`

### Verify

- [ ] Open post from feed
- [ ] `?focusComments=1` focuses composer
- [ ] List comments with pagination if applicable
- [ ] Post new comment
- [ ] Reply to comment

---

## 7) Phase 4 — Post actions

**Goal:** Like, unlike, save, unsave, delete; likers list.

### Port

| Category | Files |
|----------|-------|
| API | `likesApi.js`, `savePostApi.js` |
| Hooks | `usePostActions.js`, `useLikesListModal.js` → screen navigation |
| Components | `LikeCountButton.jsx`, update `PostCard.jsx` action bar |

### Optional route

| File |
|------|
| `app/post/[postId]/likes.jsx` |

### API behavior docs

- `LikeUnlikePost-api-and-behavior.md`
- `SaveUnsavePost-api-and-behavior.md`
- `DeletePost-api-and-behavior.md`
- `ViewPostLikers-api-and-behavior.md`
- `LikeComment-api-and-behavior.md`
- `DeleteOwnComment-api-and-behavior.md`

### Verify

- [ ] Optimistic or refetch like/save state
- [ ] Delete own post returns to feed
- [ ] View likers list

---

## 8) Phase 5 — Create / edit post

**Goal:** Create with media upload, edit, draft/publish.

### Create routes

| File |
|------|
| `app/post/create.jsx` |
| `app/post/[postId]/edit.jsx` |

### Port

| Category | Files |
|----------|-------|
| API | `createPostApi.js`, `editPostApi.js`, upload endpoints |
| Hooks | `useCreatePost.js`, `useEditPost.js`, `usePostMediaUpload.js`, `useCreatePostModal` logic → navigation |
| Components | `FeedComposer.jsx`, `SocialWriteBlockedBanner.jsx`, form fields from `PostFormModal.jsx` |

### Native adapters

- `expo-image-picker` for gallery/camera
- Multipart upload per `UploadPostMedia-api-and-behavior.md`

### Hide (v1)

- Crop, filter, location (see scope doc)

### API behavior docs

- `CreatePost-api-and-behavior.md`
- `EditPost-api-and-behavior.md`
- `UploadPostMedia-api-and-behavior.md`
- `EnforceUserStatusOnWrite-api-and-behavior.md`

### Verify

- [ ] Open create from feed composer
- [ ] Upload image + publish → appears on global feed
- [ ] Edit caption on own post
- [ ] Suspended user sees blocked banner / error

---

## 9) Phase 6 — Profile + follow

**Goal:** View any user profile, follow/unfollow, followers/following, user post grid.

### Create routes

| File |
|------|
| `app/user/[userId]/index.jsx` |
| `app/user/[userId]/followers.jsx` (tabs: followers / following) |
| Update `app/(tabs)/profile.jsx` → redirect to current user id |

### Port

| Category | Files |
|----------|-------|
| API | `profileApi.js`, `userPostsApi.js`, `followApi.js`, `relationsApi.js` |
| Hooks | `useSocialProfile.js`, `usePublicUserProfile.js`, `useUserPosts.js`, `useFollowActions.js`, `useFollowListModal.js` |
| Components | `ProfileHero.jsx`, `ProfilePostTile.jsx`, `ProfilePostsFilter.jsx` |
| Utils | `resolveProfileDetails.js`, `socialProfileRoutes.js` |

### API behavior docs

- `ViewSocialProfile-api-and-behavior.md`
- `ViewUserPosts-api-and-behavior.md`
- `FollowUser-api-and-behavior.md`
- `UnfollowUser-api-and-behavior.md`
- `ViewFollowersFollowingList-api-and-behavior.md`

### Verify

- [ ] Self profile shows draft filter
- [ ] Other user profile respects `canViewFullProfile`
- [ ] Follow / unfollow updates button state
- [ ] Post grid opens post detail
- [ ] Followers and following lists

---

## 10) Phase 7 — Secondary screens

**Goal:** Parity with remaining web pages.

### 7a Saved posts

| Route | `app/saved.jsx` |
| API | `savedPostsApi.js` |
| Web | `SocialSavedPostsPage.jsx` |
| Doc | `ViewSavedPosts-api-and-behavior.md` |

### 7b Search posts

| Route | `app/search.jsx` |
| API | `searchPostsApi.js` |
| Hook | `useSearchPosts.js` |
| Web | `SocialSearchPostsPage.jsx` |
| Doc | `SearchPost-api-and-behavior.md` |

### 7c Hashtag posts

| Route | `app/tags/[hashtag].jsx` |
| API | `searchHashtagApi.js` |
| Hook | `useHashtagPosts.js` |
| Web | `SocialHashtagPostsPage.jsx` |
| Doc | `SearchHashtag-api-and-behavior.md` |

### 7d Suggested users

| Route | `app/suggestions.jsx` |
| API | `discoveryApi.js` |
| Hook | `useSuggestedUsers.js`, `useSuggestedUsersPage.js` |
| Web | `SocialSuggestedUsersPage.jsx` |
| Doc | `ViewSuggestedUsers-api-and-behavior.md` |

### 7e Feed discovery section

| Component | `FeedDiscoverySection.jsx` |
| Hooks | `useTrendingHashtags.js`, `useSuggestedUsers.js` (limit 3) |
| Web | `FeedRightSidebar.jsx` |
| Doc | `ViewTrendingHashtags-api-and-behavior.md` |

### Verify each

- [ ] Navigation entry from feed header or discovery
- [ ] Loading / error / empty
- [ ] Pagination where web paginates

---

## 11) Phase 8 — Polish and cross-module

| Task | Notes |
|------|-------|
| Product tags in posts | Port `postProductTagApi.js`, `PostProductTagsBlock.jsx`; link to `/(tabs)/shop/product/[id]` when commerce exists |
| Comment image upload | `useCommentMediaUpload.js` + `expo-image-picker` |
| Video in feed | Port `VideoPlaybackContext`, `postMediaType.js`, `expo-av` / `expo-video` |
| Toasts | Centralize success messages from web (`Đăng bài thành công.`, etc.) |
| `useFeedSidebarStats` | Optional on profile header (post/follower/saved counts) |
| Deep links | Wire `twohands://post/:id`, `twohands://user/:id` |

---

## 12) Suggested AI task breakdown (one PR each)

| # | Task | Deliverable |
|---|------|-------------|
| 1 | Phase 0 | `socialApiClient` + `feedApi` + smoke test |
| 2 | Phase 1 | Tab shell + auth gate |
| 3 | Phase 2 | Feed read-only |
| 4 | Phase 3 | Post detail + comments |
| 5 | Phase 4 | Like / save / delete |
| 6 | Phase 5 | Create / edit post |
| 7 | Phase 6 | Profile + follow |
| 8 | Phase 7a–d | Saved, search, hashtag, suggestions (can split) |
| 9 | Phase 7e + 8 | Discovery section + polish |

---

## 13) Testing checklist (manual on emulator)

1. Start auth-service (3001) + social-service (3002).
2. Login with test user.
3. Feed loads global and following tabs.
4. Open post, comment, like, save.
5. Create post with photo.
6. View own profile and another user's profile; follow/unfollow.
7. Saved, search, hashtag, suggestions screens.
8. Logout and confirm feed is inaccessible.

---

## 14) Files not to create on mobile

| Web-only | Reason |
|----------|--------|
| `FeedLeftSidebar.jsx` as sidebar | Use profile tab |
| `FeedRightSidebar.jsx` as sidebar | Use `FeedDiscoverySection` |
| DOM hooks (`useHorizontalScrollDrag`) | Not applicable |
| Duplicate `docs/api_fe_behavior/*` | Shared repo docs |

---

## 15) Related documents

| Document | When to read |
|----------|--------------|
| `mobile/docs/mobile-social-scope.md` | Before any social task — in/out of scope |
| `mobile/docs/mobile-social-ui-map.md` | When implementing a specific screen |
| `mobile/docs/mobile-convention.md` | Naming, folder layout |
| `mobile/docs/mobile-api-integration.md` | axios, unwrap, 401 |
| `mobile/docs/mobile-design-system.md` | Colors, spacing |
| `mobile/AGENTS.md` | Agent entry point (update to link these 3 files) |

---

## 16) Prompt template (full social feature)

```text
Implement Phase [N] of mobile social per mobile/docs/mobile-social-implementation-order.md.

Read first:
- mobile/docs/mobile-social-scope.md
- mobile/docs/mobile-social-ui-map.md
- mobile/docs/mobile-social-implementation-order.md (Phase [N])
- mobile/docs/mobile-convention.md
- mobile/docs/mobile-api-integration.md
- docs/api_fe_behavior/social_api_fe_behavior/[relevant].md

Port from frontend/src/fe-module/features/social/ where listed in implementation order.
Do not modify backend. UTF-8 files only. No API calls in app/*.jsx.
```
