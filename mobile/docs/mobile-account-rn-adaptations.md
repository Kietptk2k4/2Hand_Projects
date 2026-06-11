# Mobile Account RN Adaptations - 2Hands

Version: 1.0
Owner: Mobile Team
Purpose: Document how to port web account settings UI/logic to React Native — what to change, what to skip, and which Expo APIs to use.

Read together with `mobile/docs/mobile-account-ui-map.md` and `frontend/src/fe-module/features/auth/account/`.

---

## 1) Mindset

| Web | Mobile |
|-----|--------|
| Reference for **business flow**, API calls, error messages | Implementation target |
| Tailwind + sidebar tabs on one page | Hub menu + **stack screens** per tab |
| `AccountPage` tab state | expo-router routes under `app/account/` |
| Copy-paste JSX | Port **hooks + api + schemas**; rebuild **components** |

---

## 2) Layout & navigation

### 2.1 Sidebar tabs → hub + stack

**Web:** `AccountPage` keeps `activeTab` in `useState` and swaps tab components inside `AccountSettingsLayout`.

**Mobile:**

- `/account` — `ScrollView` or `FlatList` of `AccountMenuRow`
- Each row → `router.push("/account/edit")` etc.
- Register `app/account/_layout.jsx` as `Stack` with header back button

**Do not** port `AccountTabNav` as a persistent sidebar — phones have no horizontal space.

### 2.2 Alerts and toasts

**Web:** `AuthAlert` at top of `AccountPage` via `onNotify` callback.

**Mobile:**

- Prefer existing `useSocialToast()` from `SocialToastProvider`
- Map variants: `success` | `error` | `info`
- Keep exact Vietnamese strings from web tabs

### 2.3 Account vs social profile

| Screen | Route | Purpose |
|--------|-------|---------|
| Social profile | `/(tabs)/profile` or `/profile/[userId]` | Posts, follow, public view |
| Account hub | `/account` | Settings menu |

Entry: self `ProfileScreen` header → settings icon → `/account`.

---

## 3) Forms

### 3.1 Text inputs

**Web:** `AccountTextInput` + `onChange` with `event.target.value`.

**Mobile:**

```javascript
<TextInput
  value={form.display_name}
  onChangeText={(text) => updateField("display_name", text)}
  placeholderTextColor={colors.onSurfaceVariant}
/>
```

Use `AccountTextInput.jsx` wrapper for consistent border, label, error text.

### 3.2 Edit profile validation

Port `validateEditProfileForm` **unchanged** from `accountSchemas.js` — same rules, same error keys.

Map server errors with web `resolveFieldErrors` pattern (`errors[].field` → input).

### 3.3 Social links editor

**Web:** dynamic rows with `<select>` for platform.

**Mobile:** use `@react-native-picker/picker` or a simple `Pressable` + modal list for platform selection.

Max 10 rows — disable **Thêm liên kết** at limit.

### 3.4 Keyboard

Wrap long forms in `KeyboardAvoidingView` + `ScrollView`:

```javascript
<KeyboardAvoidingView behavior={Platform.OS === "ios" ? "padding" : undefined}>
  <ScrollView keyboardShouldPersistTaps="handled">
    <EditProfileForm />
  </ScrollView>
</KeyboardAvoidingView>
```

---

## 4) Avatar upload

### 4.1 File picker

**Web:** `<input type="file" accept="image/*">`.

**Mobile:** `expo-image-picker`:

```javascript
const result = await ImagePicker.launchImageLibraryAsync({
  mediaTypes: ImagePicker.MediaTypeOptions.Images,
  allowsEditing: true,
  aspect: [1, 1],
  quality: 0.9,
});
```

Map MIME from `result.assets[0].mimeType` — validate against `AVATAR_ALLOWED_TYPES`.

### 4.2 Upload to presigned URL

**Web:** `XMLHttpRequest` or `fetch` with progress events.

**Mobile:** `fetch(presignedUrl, { method: "PUT", body: blob, headers: { "Content-Type": mime } })`.

Progress bar is optional v1; spinner is acceptable.

### 4.3 Preview

**Web:** `URL.createObjectURL(file)`.

**Mobile:** use `result.assets[0].uri` directly in `<Image source={{ uri }} />`.

Revoke object URLs not needed on RN.

---

## 5) Privacy toggle

**Web:** custom toggle or checkbox with async save on click.

**Mobile:** `Switch` from `react-native`:

```javascript
<Switch
  value={isPrivate}
  onValueChange={onToggle}
  disabled={isSaving}
  trackColor={{ true: colors.primary }}
/>
```

Keep optimistic update with rollback on error (same as web `PrivacyTab`).

---

## 6) Appearance settings

**Web:** radio cards + `AppearanceContext.setAppearanceMode`.

**Mobile options:**

1. Three `Pressable` cards (recommended — matches stitch)
2. `SegmentedControl` if available

Port `normalizeAppearanceMode` from web. After save:

- Persist to API via `updateMySettings`
- Apply theme via context + `useColorScheme()` for `SYSTEM`

---

## 7) Delete account modal

**Web:** custom modal component in `DeleteAccountTab`.

**Mobile:**

- `Modal` from `react-native` with transparent backdrop, or
- `Alert.alert` for simple confirm (less ideal for password re-entry flow)

Prefer `Modal` to match two-step flow: password on screen → confirm dialog.

Use `secureTextEntry` on password `TextInput`.

---

## 8) Data fetching

### 8.1 useAccountProfile

**Web:** `useState` + `useEffect` + manual `load()`.

**Mobile (recommended):** React Query — consistent with social module:

```javascript
export function useAccountProfile() {
  return useQuery({
    queryKey: accountKeys.me(),
    queryFn: getMyProfile,
  });
}
```

Handle 401 in query `meta` or global error boundary — match web `showSessionExpired`.

### 8.2 Mutations

Use `useMutation` for edit profile, avatar, privacy, settings, delete.

`onSuccess`: `queryClient.invalidateQueries({ queryKey: accountKeys.me() })` + social profile keys.

---

## 9) Styling

### 9.1 AccountCard

Port visual from `authUi.jsx` `AccountCard`:

- Background: `colors.surfaceContainerLowest`
- Border radius: 12–16
- Padding: 16
- Section title: `fontSize 18`, `fontWeight 600`, `colors.onSurface`

### 9.2 Danger zone

Delete screen header:

- Background: `colors.errorContainer`
- Text: `colors.onErrorContainer`

### 9.3 Icons

**Web:** Material Symbols in `AccountTabNav`.

**Mobile:** `@expo/vector-icons` (Ionicons or MaterialIcons) — one icon per hub row.

---

## 10) Cache sync with social profile

After any mutation that changes `display_name`, `bio`, `avatar_url`, or `is_private`:

```javascript
queryClient.invalidateQueries({ queryKey: accountKeys.me() });
queryClient.invalidateQueries({ queryKey: profileKeys.detail(currentUserId) });
// If using fetchPublicUserProfile cache:
queryClient.invalidateQueries({ queryKey: ["publicProfile", currentUserId] });
```

This keeps `(tabs)/profile` header in sync without manual navigation.

---

## 11) What not to port

| Web | Mobile v1 |
|-----|-----------|
| `AccountSettingsLayout` sidebar | Hub menu only |
| `NotificationSettingsTab` | Defer |
| `AccountInfoTab` session links | Defer until security screen |
| `Link to={APP_ROUTES.accountPassword}` | Defer Phase 9 |
| DOM `fileInputRef` | `expo-image-picker` |
| `localStorage` token in change password | Use `tokenStorage` + authApiClient |

---

## 12) Expo / RN dependencies

| Need | Package |
|------|---------|
| Image pick | `expo-image-picker` (already in project from social) |
| Secure storage | `expo-secure-store` (tokens) |
| Router | `expo-router` |
| Query | `@tanstack/react-query` (if used in social — match) |
| Theme | `shared/theme/colors.js` + optional appearance context |

---

## 13) UTF-8 on Windows

All new `.js`, `.jsx`, `.md` must be UTF-8 without BOM. After bulk file creation, verify:

```powershell
python -c "print(open(r'PATH', 'rb').read(20))"
```

Fix UTF-16 corruption before committing.

---

## 14) AI prompt snippet

```text
Port [AccountTab] from web to React Native per mobile/docs/mobile-account-rn-adaptations.md.

Rebuild layout with View/Text/Pressable — do not copy Tailwind classNames.
Use expo-image-picker for avatar. Use stack screens not sidebar tabs.
Match validation from accountSchemas.js. Use useSocialToast for alerts.
```

---

## Related documents

| Document | Role |
|----------|------|
| `mobile/docs/mobile-account-scope.md` | In/out of scope |
| `mobile/docs/mobile-account-ui-map.md` | Routes and components |
| `mobile/docs/mobile-account-implementation-order.md` | Build order |
| `mobile/docs/mobile-account-screen-checklist.md` | Per-screen DoD |
| `mobile/docs/mobile-design-system.md` | Tokens |
