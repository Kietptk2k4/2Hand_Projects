# Mobile Social UI Map - 2Hands

Version: 1.0  
Owner: Mobile Team  
Purpose: Map every web social surface to its mobile route, components, and design references — so AI ports layout correctly (single column, screens not modals).

---

## 1) Layout principle (web vs mobile)

| Web (`SocialFeedPage`) | Mobile |
|------------------------|--------|
| 3-column grid (`FeedLeftSidebar` + feed + `FeedRightSidebar`) | **Single column** full-width feed |
| `lg:grid-cols-12`, sidebars `hidden` below `lg` | No sidebars; use tabs + stack screens |
| `PostDetailModal` overlay | **Full screen** `app/post/[postId].jsx` |
| `CreatePostModal` / `EditPostModal` | **Stack screens** `app/post/create.jsx`, `app/post/[postId]/edit.jsx` |
| `FollowListModal` / `LikesListModal` | Stack screen or bottom sheet (prefer screen for v1 simplicity) |
| `FeedToast` | `Alert`, toast library, or inline banner — match message text from web |
| Tailwind + Material Symbols | `View` / `Text` / `Pressable` + `@expo/vector-icons` or similar |
| Infinite scroll (intersection / button) | `FlatList` + `onEndReached` |

---

## 2) Route map (expo-router)

Suggested file structure under `mobile/app/`:

```text
app/
├── _layout.jsx                          # Root stack; auth gate
├── index.jsx                            # Redirect: login or (tabs)/feed
├── (auth)/
│   └── login.jsx                        # EXISTS
├── (tabs)/
│   ├── _layout.jsx                      # Bottom tabs
│   ├── feed.jsx                         # Social home
│   └── profile.jsx                      # Current user profile (shortcut)
├── post/
│   ├── create.jsx                       # Create post
│   └── [postId]/
│       ├── index.jsx                    # Post detail + comments
│       ├── edit.jsx                     # Edit post
│       └── likes.jsx                    # Post likers (optional path)
├── user/
│   └── [userId]/
│       ├── index.jsx                    # Public / self profile
│       └── followers.jsx                # Followers / following (tab param)
├── saved.jsx                            # Saved posts
├── search.jsx                           # Search posts
├── tags/
│   └── [hashtag].jsx                    # Hashtag feed
└── suggestions.jsx                      # Suggested users
```

Deep link examples (scheme `twohands://`):

| Path | Example |
|------|---------|
| Feed | `twohands://feed` or `/(tabs)/feed` |
| Post | `twohands://post/507f1f77bcf86cd799439011` |
| Profile | `twohands://user/d7548df7-8b14-4a35-86cc-3f3e6adcbaf3` |
| Hashtag | `twohands://tags/spring` |

Define helpers in `src/shared/constants/routes.js` when implemented.

---

## 3) Page-by-page mapping

### 3.1 Feed (primary)

| | |
|--|--|
| **Web** | `pages/SocialFeedPage.jsx` |
| **Mobile route** | `app/(tabs)/feed.jsx` |
| **Stitch** | `frontend/stitch/social_feed/` (`DESIGN.md`, `screen.png`) |
| **API behavior** | `ViewGlobalFeed-api-and-behavior.md`, `ViewFollowingFeed-api-and-behavior.md` |

**Web components → mobile components:**

| Web | Mobile component | Notes |
|-----|------------------|-------|
| `FeedTabs` | `FeedTabs.jsx` | Tabs: Đề xuất / Đang theo dõi |
| `FeedComposer` | `FeedComposer.jsx` | Opens create post screen |
| `SocialWriteBlockedBanner` | `SocialWriteBlockedBanner.jsx` | Top of feed |
| `PostCard` | `PostCard.jsx` | Tap → post detail screen |
| `FeedPostSkeleton` | `FeedPostSkeleton.jsx` | Initial load |
| `FeedLeftSidebar` | **Omit** | Stats on Profile tab |
| `FeedRightSidebar` | `FeedDiscoverySection.jsx` | Trending hashtags + 2–3 suggested users + "Xem thêm" → suggestions |
| `PostDetailModal` | Navigate to `post/[postId]` | Pass `focusComments` via query `?focusComments=1` |
| `CreatePostModal` | Navigate to `post/create` | |
| `EditPostModal` | Navigate to `post/[postId]/edit` | |
| `LikesListModal` | `post/[postId]/likes` or modal phase 2 | |

**Feed empty messages (keep exact copy from web):**

- Following: `Bạn chưa theo dõi ai hoặc chưa có bài viết từ người bạn theo dõi.`
- Global: `Chưa có bài viết công khai nào trên feed đề xuất.`

---

### 3.2 Post detail

| | |
|--|--|
| **Web** | `components/PostDetailModal.jsx` |
| **Mobile route** | `app/post/[postId]/index.jsx` |
| **Stitch** | `frontend/stitch/post_detail/` |
| **API behavior** | `FR_ViewPostDetail.md`, `ListPostComments-api-and-behavior.md`, `CommentPost-api-and-behavior.md`, `LikeUnlikePost-api-and-behavior.md`, `SaveUnsavePost-api-and-behavior.md` |

**Sub-components to port:**

| Web | Mobile |
|-----|--------|
| `PostMediaStage` / `PostMediaCarousel` | `PostMediaCarousel.jsx` — horizontal `FlatList` or `ScrollView` |
| `PostCaption` | `PostCaption.jsx` |
| `PostProductTagsBlock` | `PostProductTagsBlock.jsx` → `router.push` commerce product |
| `PostDetailComments` | `PostDetailComments.jsx` |
| `CommentItem` | `CommentItem.jsx` |
| `CommentComposer` | `CommentComposer.jsx` — text + optional image |
| `CommentMediaDisplay` | `CommentMediaDisplay.jsx` |
| `MediaGalleryLightbox` | Full-screen `Modal` + image viewer |
| `LikeCountButton` | `LikeCountButton.jsx` → likers screen |

**Query params:**

- `focusComments=1` — scroll to / focus comment composer (web: `usePostDetailModal`)

---

### 3.3 Create / edit post

| | |
|--|--|
| **Web create** | `components/CreatePostModal.jsx` + `PostFormModal.jsx` |
| **Web edit** | `components/EditPostModal.jsx` |
| **Mobile routes** | `app/post/create.jsx`, `app/post/[postId]/edit.jsx` |
| **Stitch** | `frontend/stitch/create_post/`, `frontend/stitch/edit_post/` (if present) |
| **API behavior** | `CreatePost-api-and-behavior.md`, `EditPost-api-and-behavior.md`, `UploadPostMedia-api-and-behavior.md`, `TagProductInPost-api-and-behavior.md` |

**Hooks to port:** `useCreatePost.js`, `useEditPost.js`, `usePostMediaUpload.js` (adapt file picker → `expo-image-picker`).

**Hide on mobile v1 (web coming soon):**

- Crop, filter buttons
- "Thêm địa điểm" row

**Keep:**

- Caption, media picker, visibility, allow comments, product tag, publish / draft

---

### 3.4 User profile

| | |
|--|--|
| **Web** | `pages/SocialProfilePage.jsx` |
| **Mobile route** | `app/user/[userId].jsx` |
| **Tab shortcut** | `app/(tabs)/profile.jsx` → redirect to `user/{currentUserId}` |
| **Stitch** | `frontend/stitch/user_profile/` |
| **API behavior** | `ViewSocialProfile-api-and-behavior.md`, `ViewUserPosts-api-and-behavior.md`, `FollowUser`, `UnfollowUser`, `ViewFollowersFollowingList` |

**Web components → mobile:**

| Web | Mobile |
|-----|--------|
| `ProfileHero` | `ProfileHero.jsx` |
| `ProfilePortfolioSection` | `ProfilePostGrid.jsx` — grid of `ProfilePostTile` |
| `ProfilePostsFilter` | `ProfilePostsFilter.jsx` | Self only: published / draft |
| `FollowListModal` | `app/user/[userId]/followers.jsx` with tab param `followers` \| `following` |
| Post tap | Navigate to `post/[postId]` |

---

### 3.5 Saved posts

| | |
|--|--|
| **Web** | `pages/SocialSavedPostsPage.jsx` |
| **Mobile route** | `app/saved.jsx` |
| **Stitch** | `frontend/stitch/saved_post/` |
| **API behavior** | `ViewSavedPosts-api-and-behavior.md`, `SaveUnsavePost-api-and-behavior.md` |

**Entry:** Header icon on feed or profile menu (web: sidebar link "Đã lưu").

**Component:** `SavedPostCard.jsx` → opens post detail.

---

### 3.6 Search posts

| | |
|--|--|
| **Web** | `pages/SocialSearchPostsPage.jsx` |
| **Mobile route** | `app/search.jsx` |
| **Stitch** | `frontend/stitch/search_post/` |
| **API behavior** | `SearchPost-api-and-behavior.md` |

**Entry:** Search icon in feed header.

**Pattern:** Search input + debounced query + `FlatList` of `PostCard` or compact row.

---

### 3.7 Hashtag posts

| | |
|--|--|
| **Web** | `pages/SocialHashtagPostsPage.jsx` |
| **Mobile route** | `app/tags/[hashtag].jsx` |
| **API behavior** | `SearchHashtag-api-and-behavior.md` |

**Entry:** Tap hashtag in `PostCaption` or trending list.

**Web route:** `/social/tags/:hashtag` → mobile `tags/[hashtag]` (no `#` in path).

---

### 3.8 Suggested users

| | |
|--|--|
| **Web** | `pages/SocialSuggestedUsersPage.jsx` |
| **Mobile route** | `app/suggestions.jsx` |
| **API behavior** | `ViewSuggestedUsers-api-and-behavior.md`, `FollowUser`, `UnfollowUser` |

**Component:** `SuggestedUserListItem.jsx` (reuse from feed discovery section).

**Entry:** "Xem thêm" from feed discovery section.

---

## 4) Bottom tab bar (MVP)

Suggested tabs for authenticated shell:

| Tab | Route | Icon label |
|-----|-------|------------|
| Feed | `(tabs)/feed` | Trang chủ / Feed |
| Shop | `(tabs)/shop` | Commerce — **placeholder until commerce module** |
| Profile | `(tabs)/profile` | Hồ sơ |

Social-specific screens (saved, search, suggestions, hashtag, post detail) live **above** tabs in root stack, not as tabs.

---

## 5) Component library (`src/features/social/components/`)

Port from web incrementally; minimum set per phase:

| Component | Used in |
|-----------|---------|
| `PostCard` | Feed, search, hashtag, profile grid context |
| `PostMediaCarousel` | PostCard, post detail |
| `FeedTabs` | Feed |
| `FeedComposer` | Feed |
| `FeedPostSkeleton` | Feed, lists |
| `FeedDiscoverySection` | Feed (replaces right sidebar) |
| `SocialWriteBlockedBanner` | Feed, create post |
| `PostCaption` | PostCard, detail |
| `PostProductTagsBlock` | PostCard, detail |
| `CommentItem` | Post detail |
| `CommentComposer` | Post detail |
| `ProfileHero` | Profile |
| `ProfilePostTile` | Profile grid |
| `SuggestedUserListItem` | Feed discovery, suggestions |
| `SavedPostCard` | Saved posts |
| `LikeCountButton` | PostCard, detail |

---

## 6) Hooks map (port from web)

| Web hook | Mobile usage |
|----------|--------------|
| `useFeed` | Feed tabs |
| `usePostActions` | Like, save, delete |
| `usePostComments` | Post detail |
| `useCreatePost` | Create screen |
| `useEditPost` | Edit screen |
| `useSocialProfile` | Profile |
| `usePublicUserProfile` | Profile (others) |
| `useUserPosts` | Profile grid |
| `useFollowActions` | Profile, suggestions |
| `useFollowListModal` → screen state | Followers/following |
| `useLikesListModal` → screen | Post likers |
| `useTrendingHashtags` | Feed discovery |
| `useSuggestedUsers` | Feed discovery + suggestions page |
| `useHashtagPosts` | Hashtag screen |
| `useSearchPosts` | Search screen |
| `useSavedPosts` | Saved screen |
| `useCommentMediaUpload` | Comment composer — `expo-image-picker` |

Do **not** port `useHorizontalScrollDrag` (DOM-specific). Use RN `ScrollView` horizontal.

`VideoPlaybackContext` — port to RN with single-active-video rule using `expo-av` or `expo-video` when implementing video in feed.

---

## 7) Web route → mobile route quick reference

| Web (`APP_ROUTES`) | Mobile |
|--------------------|--------|
| `/social` | `/(tabs)/feed` |
| `/social/saved` | `/saved` |
| `/social/suggestions` | `/suggestions` |
| `/social/search` | `/search` |
| `/social/tags/:hashtag` | `/tags/[hashtag]` |
| `/social/users/:userId` | `/user/[userId]` |
| Post detail (modal) | `/post/[postId]` |
| Create post (modal) | `/post/create` |
| Edit post (modal) | `/post/[postId]/edit` |

---

## 8) Design tokens

Primary visual source for social screens:

1. `frontend/stitch/social_feed/DESIGN.md` — colors, typography (use `headline-*-mobile` scales)
2. `mobile/src/shared/theme/colors.js`
3. `mobile/docs/mobile-design-system.md`

Stitch folders per screen listed in sections above. Prefer `screen.png` for layout; `code.html` is reference only (do not copy HTML/CSS).

---

## 9) AI prompt snippet (per screen)

```text
Implement mobile screen: [ROUTE]

Read:
- mobile/docs/mobile-social-scope.md
- mobile/docs/mobile-social-ui-map.md (section [X])
- docs/api_fe_behavior/social_api_fe_behavior/[API].md
- frontend/src/fe-module/features/social/pages/[WebPage].jsx

Stitch: frontend/stitch/[folder]/
Use FlatList, theme colors, no API in app/*.jsx.
```
