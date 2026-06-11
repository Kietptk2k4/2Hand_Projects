# Mobile Social Screen Checklist - 2Hands

Version: 1.0  
Owner: Mobile Team  
Purpose: Definition of Done per social screen — loading, error, empty, pagination, auth, and navigation — so AI and reviewers verify each screen before merge.

---

## How to use

1. Pick one screen from the table below.
2. Read its **Web source**, **API behavior** docs, and **Stitch** folder.
3. Implement route under `app/` + logic under `src/features/social/`.
4. Check every box in **States**, **Actions**, and **Quality** before marking done.
5. Cross-check `mobile/docs/mobile-social-ui-map.md` for component mapping.

**Global rules (all screens):**

- [ ] No `axios` / `fetch` in `app/*.jsx`
- [ ] JWT attached via `socialApiClient` (not manual headers in screens)
- [ ] Response unwrapped per `mobile/docs/mobile-api-integration.md`
- [ ] Colors from `src/shared/theme/colors.js`
- [ ] UTF-8 encoding on new files (Windows)
- [ ] Vietnamese copy matches web where specified below

---

## Screen index

| # | Screen | Route | Phase |
|---|--------|-------|-------|
| 1 | Feed | `app/(tabs)/feed.jsx` | 2 |
| 2 | Post detail | `app/post/[postId]/index.jsx` | 3 |
| 3 | Create post | `app/post/create.jsx` | 5 |
| 4 | Edit post | `app/post/[postId]/edit.jsx` | 5 |
| 5 | Post likers | `app/post/[postId]/likes.jsx` | 4 |
| 6 | Profile | `app/user/[userId]/index.jsx` | 6 |
| 7 | Followers / following | `app/user/[userId]/followers.jsx` | 6 |
| 8 | Saved posts | `app/saved.jsx` | 7 |
| 9 | Search posts | `app/search.jsx` | 7 |
| 10 | Hashtag posts | `app/tags/[hashtag].jsx` | 7 |
| 11 | Suggested users | `app/suggestions.jsx` | 7 |

---

## 1) Feed

| Field | Value |
|-------|-------|
| **Route** | `app/(tabs)/feed.jsx` |
| **Web** | `frontend/src/fe-module/features/social/pages/SocialFeedPage.jsx` |
| **Stitch** | `frontend/stitch/social_feed/` |
| **API** | `ViewGlobalFeed-api-and-behavior.md`, `ViewFollowingFeed-api-and-behavior.md` |
| **Hooks** | `useFeed.js` |
| **Components** | `FeedTabs`, `FeedComposer`, `PostCard`, `FeedPostSkeleton`, `FeedDiscoverySection`, `SocialWriteBlockedBanner` |

### States

- [ ] **Loading (initial):** 2x `FeedPostSkeleton` (or list skeleton)
- [ ] **Loading more:** footer `ActivityIndicator` when `onEndReached`
- [ ] **Error:** message + button **Thử lại** (calls `retry`)
- [ ] **Empty — Global:** `Chưa có bài viết công khai nào trên feed đề xuất.`
- [ ] **Empty — Following:** `Bạn chưa theo dõi ai hoặc chưa có bài viết từ người bạn theo dõi.`
- [ ] **Ready:** `FlatList` of posts, pull-to-refresh optional (nice-to-have)

### Actions & navigation

- [ ] Tab switch Global / Following refetches feed
- [ ] Tap post → `router.push(/post/[postId])`
- [ ] Tap author avatar/name → `router.push(/user/[userId])`
- [ ] Tap hashtag → `router.push(/tags/[hashtag])`
- [ ] Composer → `router.push(/post/create)`
- [ ] Header: search → `/search`, saved → `/saved` (or icons)
- [ ] Discovery section: trending hashtags, 2–3 suggestions, **Xem thêm** → `/suggestions`
- [ ] **No share button** (out of scope v1) or show coming-soon message

### Auth

- [ ] Requires login; unauthenticated user never sees feed

### Quality

- [ ] Page size 20 (`FEED_PAGE_SIZE`)
- [ ] `hasNext` / meta respected for pagination
- [ ] 401 triggers session expired flow (via hook / auth layer)

---

## 2) Post detail

| Field | Value |
|-------|-------|
| **Route** | `app/post/[postId]/index.jsx` |
| **Web** | `components/PostDetailModal.jsx`, `PostDetailComments.jsx` |
| **Stitch** | `frontend/stitch/post_detail/` |
| **API** | `FR_ViewPostDetail.md`, `ListPostComments-api-and-behavior.md`, `CommentPost-api-and-behavior.md`, `ReplyComment-api-and-behavior.md`, `LikeUnlikePost`, `SaveUnsavePost` |
| **Params** | `postId`; optional `focusComments=1` |

### States

- [ ] **Loading:** skeleton for media + caption + comments
- [ ] **Error:** not found / network + retry
- [ ] **Empty comments:** inline hint (web pattern)
- [ ] **Comments loading more:** if paginated

### Actions

- [ ] Like / unlike post
- [ ] Save / unsave post
- [ ] Open likers → `/post/[postId]/likes`
- [ ] Add comment (text)
- [ ] Reply to comment
- [ ] Like comment (if on web)
- [ ] Delete own comment
- [ ] Tap product tag → commerce product route (stub OK until commerce exists)
- [ ] `focusComments=1` scrolls to / focuses composer
- [ ] Back navigation returns to previous screen

### Auth

- [ ] Login required

### Quality

- [ ] Media carousel horizontal scroll
- [ ] Image lightbox via `Modal` (port of `MediaGalleryLightbox`)
- [ ] Keyboard does not cover comment composer (`KeyboardAvoidingView`)

---

## 3) Create post

| Field | Value |
|-------|-------|
| **Route** | `app/post/create.jsx` |
| **Web** | `CreatePostModal.jsx`, `PostFormModal.jsx` |
| **Stitch** | `frontend/stitch/create_post/` |
| **API** | `CreatePost-api-and-behavior.md`, `UploadPostMedia-api-and-behavior.md`, `TagProductInPost-api-and-behavior.md` |

### States

- [ ] **Idle:** empty form
- [ ] **Uploading:** progress / spinner on media
- [ ] **Submitting:** disable publish button
- [ ] **Error:** field or banner (incl. suspended user)
- [ ] **Success:** toast `Đăng bài thành công.` or draft `Đã lưu bản nháp.` then navigate back

### Actions

- [ ] Pick image(s) via `expo-image-picker`
- [ ] Caption, visibility, allow comments
- [ ] Product tag (if web supports)
- [ ] Publish and save draft
- [ ] **Hide:** crop, filter, location (v1)

### Auth

- [ ] Login required
- [ ] `SocialWriteBlockedBanner` / suspended error from API

---

## 4) Edit post

| Field | Value |
|-------|-------|
| **Route** | `app/post/[postId]/edit.jsx` |
| **Web** | `EditPostModal.jsx` |
| **API** | `EditPost-api-and-behavior.md`, `UploadPostMedia-api-and-behavior.md` |

### States

- [ ] **Loading:** load existing post into form
- [ ] **Error:** load fail + save fail
- [ ] **Success:** `Cập nhật bài viết thành công.` then go back

### Actions

- [ ] Only author can edit (match web ownership checks)
- [ ] Update caption, media, settings per API

### Auth

- [ ] Login + own post only

---

## 5) Post likers

| Field | Value |
|-------|-------|
| **Route** | `app/post/[postId]/likes.jsx` |
| **Web** | `LikesListModal.jsx`, `LikesListRow.jsx` |
| **API** | `ViewPostLikers-api-and-behavior.md` |

### States

- [ ] Loading, error + retry, empty list
- [ ] Pagination if API paginates

### Actions

- [ ] Tap user → `/user/[userId]`

---

## 6) Profile

| Field | Value |
|-------|-------|
| **Route** | `app/user/[userId]/index.jsx` |
| **Tab** | `(tabs)/profile.jsx` → redirect to current user |
| **Web** | `SocialProfilePage.jsx` |
| **Stitch** | `frontend/stitch/user_profile/` |
| **API** | `ViewSocialProfile-api-and-behavior.md`, `ViewUserPosts-api-and-behavior.md`, `FollowUser`, `UnfollowUser` |

### States

- [ ] **Loading:** hero + grid skeleton
- [ ] **Error:** profile not found / private / network
- [ ] **Empty posts:** message when grid has no items
- [ ] **Self vs other:** correct follow button / edit affordances

### Actions

- [ ] Follow / unfollow (not on self)
- [ ] Open followers / following screen
- [ ] Post grid tap → post detail
- [ ] Self: filter published / draft (`ProfilePostsFilter`)
- [ ] Edit own post from grid/menu

### Auth

- [ ] Login required to view (match web)

---

## 7) Followers / following

| Field | Value |
|-------|-------|
| **Route** | `app/user/[userId]/followers.jsx` |
| **Web** | `FollowListModal.jsx` |
| **API** | `ViewFollowersFollowingList-api-and-behavior.md` |

### States

- [ ] Tab or param: `followers` | `following`
- [ ] Loading, error, empty per tab
- [ ] Pagination if applicable

### Actions

- [ ] Tap row → user profile
- [ ] Follow back from list (if web does)

---

## 8) Saved posts

| Field | Value |
|-------|-------|
| **Route** | `app/saved.jsx` |
| **Web** | `SocialSavedPostsPage.jsx` |
| **Stitch** | `frontend/stitch/saved_post/` |
| **API** | `ViewSavedPosts-api-and-behavior.md`, `SaveUnsavePost-api-and-behavior.md` |

### States

- [ ] Loading, error + retry, empty saved list
- [ ] Pagination

### Actions

- [ ] Tap card → post detail
- [ ] Unsave from card (if web supports inline)

---

## 9) Search posts

| Field | Value |
|-------|-------|
| **Route** | `app/search.jsx` |
| **Web** | `SocialSearchPostsPage.jsx` |
| **Stitch** | `frontend/stitch/search_post/` |
| **API** | `SearchPost-api-and-behavior.md` |

### States

- [ ] Initial (no query): hint or recent placeholder
- [ ] Loading, error, no results
- [ ] Results list with pagination

### Actions

- [ ] Debounced search input
- [ ] Tap result → post detail

---

## 10) Hashtag posts

| Field | Value |
|-------|-------|
| **Route** | `app/tags/[hashtag].jsx` |
| **Web** | `SocialHashtagPostsPage.jsx` |
| **API** | `SearchHashtag-api-and-behavior.md` |

### States

- [ ] Header shows `#hashtag` (normalized, no double `#`)
- [ ] Loading, error, empty, pagination

### Actions

- [ ] Post tap → detail
- [ ] Hashtag in caption navigates here

---

## 11) Suggested users

| Field | Value |
|-------|-------|
| **Route** | `app/suggestions.jsx` |
| **Web** | `SocialSuggestedUsersPage.jsx` |
| **API** | `ViewSuggestedUsers-api-and-behavior.md`, `FollowUser`, `UnfollowUser` |

### States

- [ ] Loading, error, empty
- [ ] Pagination / load more if web paginates

### Actions

- [ ] Follow / unfollow per row
- [ ] Tap avatar → profile

---

## Reviewer quick pass (any screen)

| Check | Pass? |
|-------|-------|
| Matches `mobile-social-scope.md` (no out-of-scope features) | |
| Matches `mobile-social-ui-map.md` route | |
| All four states handled where applicable | |
| Tested on Android emulator with social-service :3002 | |
| No secrets in logs | |

---

## Related documents

| Document | Role |
|----------|------|
| `mobile/docs/mobile-social-scope.md` | In/out of scope |
| `mobile/docs/mobile-social-ui-map.md` | Route + component map |
| `mobile/docs/mobile-social-implementation-order.md` | Build phase |
| `mobile/docs/mobile-social-rn-adaptations.md` | Web → RN patterns |
| `mobile/docs/mobile-design-system.md` | Visual tokens |
