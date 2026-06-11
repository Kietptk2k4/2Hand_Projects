# Mobile Social RN Adaptations - 2Hands

Version: 1.0  
Owner: Mobile Team  
Purpose: Document how to port web social UI/logic to React Native — what to change, what to skip, and which Expo APIs to use.

Read together with `mobile/docs/mobile-social-ui-map.md` and `frontend/src/fe-module/features/social/`.

---

## 1) Mindset

| Web | Mobile |
|-----|--------|
| Reference for **business flow**, API calls, error messages | Implementation target |
| Tailwind + HTML + modal overlays | `View`, `Text`, `Pressable`, `FlatList`, full screens |
| Desktop 3-column feed | Single column; sidebars become sections or separate screens |
| Copy-paste JSX | Port **hooks + api**; rebuild **components** |

---

## 2) Layout & navigation

### 2.1 Feed layout

**Web:** `SocialFeedPage` uses `lg:grid-cols-12` with `FeedLeftSidebar` (hidden on small screens) + center feed + `FeedRightSidebar`.

**Mobile:**

- One `FlatList` with `ListHeaderComponent` = tabs + composer + discovery section
- **Do not** port `FeedLeftSidebar` as a column — move stats to Profile tab
- **Do not** port sticky right sidebar — use `FeedDiscoverySection` inline in header

### 2.2 Modals → screens

| Web modal | Mobile |
|-----------|--------|
| `PostDetailModal` | `app/post/[postId]/index.jsx` |
| `CreatePostModal` | `app/post/create.jsx` |
| `EditPostModal` | `app/post/[postId]/edit.jsx` |
| `FollowListModal` | `app/user/[userId]/followers.jsx` |
| `LikesListModal` | `app/post/[postId]/likes.jsx` |
| `MediaGalleryLightbox` | RN `Modal` + full-screen `Image` |

Use `router.push()` / `router.back()` from `expo-router`. Pass query params instead of modal state (e.g. `focusComments=1`).

### 2.3 Toast

**Web:** `FeedToast` component.

**Mobile options:**

- Simple: `Alert.alert` for errors only
- Better: `react-native-toast-message` or inline banner `View`
- **Keep exact Vietnamese strings** from web (`Đăng bài thành công.`, etc.)

---

## 3) Lists & pagination

### 3.1 Replace infinite scroll patterns

**Web:** scroll listeners, load-more buttons, or intersection observer.

**Mobile:**

```javascript
<FlatList
  data={items}
  keyExtractor={(item) => item.postId}
  renderItem={({ item }) => <PostCard post={item} />}
  onEndReached={loadMore}
  onEndReachedThreshold={0.4}
  ListFooterComponent={isLoadingMore ? <ActivityIndicator /> : null}
/>
```

- Use `FEED_PAGE_SIZE` (20) from `constants/feedTabs.js`
- Guard `loadMore` with `hasNext` / `meta` from API
- Debounce `onEndReached` (avoid double fetch) — see web `useFeed` `loadPage` pattern

### 3.2 Pull to refresh

Optional v1 enhancement:

```javascript
<FlatList refreshing={isRefreshing} onRefresh={refetch} />
```

Port `refetch` from `useFeed` / equivalent hooks.

### 3.3 Profile post grid

**Web:** CSS grid of `ProfilePostTile`.

**Mobile:** `FlatList` with `numColumns={3}` or `FlashList` with grid — keep square tiles, `gap` via margin.

---

## 4) Styling

### 4.1 No Tailwind on mobile

| Web | Mobile |
|-----|--------|
| `className="rounded-xl border..."` | `StyleSheet.create` or small shared styles |
| `material-symbols-outlined` | `@expo/vector-icons` (MaterialIcons / Ionicons) |
| `hover:` states | `Pressable` `style={({ pressed }) => ...}` |

Use tokens from `src/shared/theme/colors.js` and `mobile/docs/mobile-design-system.md`.

### 4.2 Cards & touch targets

- Card: `borderRadius: 16`, `padding: 16`, background `surfaceContainerLowest`
- Minimum touch target: **44×44**
- Primary button: `minHeight: 48`, `borderRadius: 8`

---

## 5) Media

### 5.1 Images in feed

| Web | Mobile |
|-----|--------|
| `<img src={url}>` | `expo-image` `Image` (caching, better perf) or RN `Image` |
| `readMediaDimensions.js` | `Image.getSize()` or expo-image metadata |
| `postMediaAspectRatio.js` | Same logic; apply to `aspectRatio` style |

### 5.2 Image picker (create post / comment)

**Web:** `<input type="file">` in `PostFormModal` / `CommentComposer`.

**Mobile:**

- `expo-image-picker` — `launchImageLibraryAsync`, `launchCameraAsync`
- Request permissions per Expo docs
- Upload via multipart per `UploadPostMedia-api-and-behavior.md`
- Port `usePostMediaUpload.js` / `useCommentMediaUpload.js` — replace `File` with picker result URI

### 5.3 Video

**Web:** HTML5 `<video>`, `VideoPlaybackContext` (single active player).

**Mobile:**

- Wrap feed stack in `VideoPlaybackProvider` (port context logic from `VideoPlaybackContext.jsx`)
- Use `expo-video` (SDK 52+) or `expo-av` `Video` component
- Map `registerPlayer` / `claimPlayback` to ref-based pause/play
- Autoplay in feed: only when item visible (optional phase 8 — start with tap-to-play)

### 5.4 Lightbox

**Web:** `MediaGalleryLightbox.jsx` with portal.

**Mobile:**

```javascript
<Modal visible={isOpen} transparent animationType="fade">
  <Pressable style={styles.backdrop} onPress={close}>
    <Image source={{ uri }} style={styles.fullImage} resizeMode="contain" />
  </Pressable>
</Modal>
```

---

## 6) Hooks & utilities — port vs skip

### Port (adapt imports only)

| Web file | Notes |
|----------|-------|
| `api/*.js` | Same endpoints; swap client to `socialApiClient` |
| `useFeed.js` | Replace `useAuthSession` import with mobile auth hook |
| `usePostActions.js` | Same optimistic/refetch logic |
| `usePostComments.js` | Same |
| `useCreatePost.js`, `useEditPost.js` | Wire picker upload |
| `useSocialProfile.js`, `useUserPosts.js` | Same |
| `useFollowActions.js` | Same |
| `formatSocialCount.js`, `mapCommentItem.js` | Copy as-is |
| `socialWriteErrors.js` | Same error codes |

### Skip (DOM-specific)

| Web file | Reason |
|----------|--------|
| `useHorizontalScrollDrag.js` | Mouse drag on web carousel |
| `usePostDetailModal.js` | Replace with route + `useLocalSearchParams` |
| `useCreatePostModal.js`, `useEditPostModal.js` | Replace with navigation |
| `useFollowListModal.js`, `useLikesListModal.js` | Replace with screens |

### Replace pattern (modal → route)

**Web:**

```javascript
const { openPost, closePost, postId } = usePostDetailModal();
openPost(id, { focusComments: true });
```

**Mobile:**

```javascript
import { router } from "expo-router";
router.push({ pathname: "/post/[postId]", params: { postId: id, focusComments: "1" } });
```

---

## 7) Forms & keyboard

- Wrap comment composer and create post in `KeyboardAvoidingView` (`behavior="padding"` on iOS, `height` on Android)
- Use `TextInput` `multiline` for caption / comments
- `ScrollView` `keyboardShouldPersistTaps="handled"` on forms
- Safe area: `SafeAreaView` from `react-native-safe-area-context` (Expo includes it)

---

## 8) Auth & HTTP

Same pattern as web `socialApiClient.js`:

1. Request interceptor: attach `Authorization: Bearer`
2. Response interceptor: 401 → refresh once via `authRefreshService.js`
3. Suspended user writes: port `isSuspendedWriteError` + banner

Mobile client path: `src/services/http/socialApiClient.js`  
Env: `EXPO_PUBLIC_SOCIAL_SERVICE_BASE_URL`

---

## 9) Commerce cross-links

**Web:** `useViewCommerceProduct` → `navigate(APP_ROUTES.commerceProductDetail)`.

**Mobile (when commerce exists):**

```javascript
router.push(`/shop/product/${productId}`);
```

Until commerce is built, stub with placeholder screen or noop.

---

## 10) Features explicitly not ported (v1)

| Web UI | Mobile action |
|--------|----------------|
| Share post button | Hide or toast: `Tính năng đang được phát triển.` |
| Crop / filter in create post | Hide controls |
| Add location | Hide row |
| Sidebar links Mạng lưới / Sự kiện | Omit |

---

## 11) Testing on device / emulator

| Target | Base URL in `.env` |
|--------|-------------------|
| Android emulator | `http://10.0.2.2:3002` |
| iOS simulator | `http://localhost:3002` |
| Physical device | PC LAN IP, e.g. `http://192.168.1.x:3002` |

Verify images from CDN load (HTTPS). Cleartext HTTP only for local dev.

---

## 12) Suggested dependencies (add when implementing)

| Package | Use |
|---------|-----|
| `expo-image` | Post / avatar images |
| `expo-image-picker` | Create post, comment media |
| `expo-video` or `expo-av` | Video posts |
| `@expo/vector-icons` | Tab bar + action icons |

Install with `npx expo install <package>` for SDK 56 compatibility.

---

## 13) AI prompt add-on

When implementing any social screen, also read this file and state which adaptations apply (modal→screen, FlatList, picker, etc.).

---

## Related documents

| Document | Role |
|----------|------|
| `mobile/docs/mobile-social-screen-checklist.md` | Per-screen DoD |
| `mobile/docs/mobile-social-ui-map.md` | Routes & components |
| `mobile/docs/mobile-convention.md` | Folder naming |
| `mobile/docs/mobile-api-integration.md` | Unwrap & errors |
