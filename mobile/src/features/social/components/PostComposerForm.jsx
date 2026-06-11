import { useState } from "react";
import {
  ActivityIndicator,
  Pressable,
  ScrollView,
  StyleSheet,
  Switch,
  Text,
  TextInput,
  View,
} from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { MAX_CAPTION_LENGTH, VISIBILITY_OPTIONS } from "../constants/createPostConstants";
import { PostMediaPicker } from "./PostMediaPicker";
import { colors } from "../../../shared/theme/colors";

export function PostComposerForm({
  mode = "create",
  form,
  isBusy = false,
  isLoadingInitial = false,
}) {
  const [showVisibilityMenu, setShowVisibilityMenu] = useState(false);

  const {
    caption,
    setCaption,
    visibility,
    setVisibility,
    allowComments,
    setAllowComments,
    hashtags,
    hashtagInput,
    setHashtagInput,
    addHashtag,
    removeHashtag,
    mediaItems,
    activeMediaIndex,
    setActiveMediaIndex,
    pickAndAddMedia,
    removeMedia,
    globalError,
    fieldErrors,
  } = form;

  const visibilityMeta = VISIBILITY_OPTIONS.find((item) => item.value === visibility);
  const disabled = isBusy || isLoadingInitial;

  return (
    <ScrollView
      style={styles.scroll}
      contentContainerStyle={styles.content}
      keyboardShouldPersistTaps="handled"
    >
      {globalError ? (
        <View style={styles.bannerError}>
          <Text style={styles.bannerErrorText}>{globalError}</Text>
        </View>
      ) : null}

      {isLoadingInitial ? (
        <View style={styles.loadingBlock}>
          <ActivityIndicator size="large" color={colors.primary} />
          <Text style={styles.loadingText}>Đang tải bài viết…</Text>
        </View>
      ) : (
        <>
          <PostMediaPicker
            mediaItems={mediaItems}
            activeMediaIndex={activeMediaIndex}
            onSelectIndex={setActiveMediaIndex}
            onAddMedia={pickAndAddMedia}
            onRemoveMedia={removeMedia}
            disabled={disabled}
            fieldError={fieldErrors.media}
          />

          <View style={styles.section}>
            <TextInput
              value={caption}
              onChangeText={setCaption}
              placeholder="Viết mô tả... Giới thiệu dịch vụ, chia sẻ mẹo hoặc đặt câu hỏi."
              placeholderTextColor={colors.outline}
              multiline
              maxLength={MAX_CAPTION_LENGTH}
              style={styles.captionInput}
              editable={!disabled}
            />
            {fieldErrors.caption ? (
              <Text style={styles.fieldError}>{fieldErrors.caption}</Text>
            ) : null}
            <Text style={styles.charCount}>
              {caption.length}/{MAX_CAPTION_LENGTH}
            </Text>
          </View>

          <View style={styles.section}>
            <Text style={styles.label}>Hashtag</Text>
            <View style={styles.hashtagRow}>
              {hashtags.map((tag) => (
                <Pressable
                  key={tag}
                  style={styles.hashtagChip}
                  onPress={() => removeHashtag(tag)}
                >
                  <Text style={styles.hashtagText}>#{tag}</Text>
                  <Ionicons name="close" size={14} color={colors.onSurfaceVariant} />
                </Pressable>
              ))}
              <TextInput
                value={hashtagInput}
                onChangeText={setHashtagInput}
                onSubmitEditing={() => addHashtag(hashtagInput)}
                onBlur={() => addHashtag(hashtagInput)}
                placeholder="Thêm hashtag..."
                placeholderTextColor={colors.outline}
                style={styles.hashtagInput}
                editable={!disabled}
                returnKeyType="done"
              />
            </View>
          </View>

          <View style={styles.section}>
            <Text style={styles.label}>Quyền hiển thị</Text>
            <Pressable
              style={styles.visibilityBtn}
              onPress={() => setShowVisibilityMenu((prev) => !prev)}
              disabled={disabled}
            >
              <Ionicons
                name={visibility === "PUBLIC" ? "earth-outline" : "people-outline"}
                size={18}
                color={colors.onSurfaceVariant}
              />
              <Text style={styles.visibilityText}>{visibilityMeta?.label}</Text>
              <Ionicons name="chevron-down" size={18} color={colors.onSurfaceVariant} />
            </Pressable>
            {showVisibilityMenu ? (
              <View style={styles.visibilityMenu}>
                {VISIBILITY_OPTIONS.map((option) => (
                  <Pressable
                    key={option.value}
                    style={styles.visibilityOption}
                    onPress={() => {
                      setVisibility(option.value);
                      setShowVisibilityMenu(false);
                    }}
                  >
                    <Text style={styles.visibilityOptionText}>{option.label}</Text>
                  </Pressable>
                ))}
              </View>
            ) : null}
          </View>

          <View style={styles.switchRow}>
            <Text style={styles.switchLabel}>Cho phép bình luận</Text>
            <Switch
              value={allowComments}
              onValueChange={setAllowComments}
              disabled={disabled}
              trackColor={{ false: colors.outlineVariant, true: colors.primary }}
            />
          </View>
        </>
      )}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  scroll: {
    flex: 1,
  },
  content: {
    padding: 16,
    gap: 16,
    paddingBottom: 32,
  },
  bannerError: {
    backgroundColor: colors.errorContainer,
    borderRadius: 8,
    padding: 12,
  },
  bannerErrorText: {
    color: colors.onErrorContainer,
    fontSize: 14,
  },
  loadingBlock: {
    paddingVertical: 48,
    alignItems: "center",
    gap: 12,
  },
  loadingText: {
    color: colors.onSurfaceVariant,
    fontSize: 14,
  },
  section: {
    gap: 8,
  },
  captionInput: {
    minHeight: 120,
    fontSize: 16,
    color: colors.onSurface,
    textAlignVertical: "top",
  },
  charCount: {
    fontSize: 12,
    color: colors.onSurfaceVariant,
    textAlign: "right",
  },
  fieldError: {
    fontSize: 12,
    color: colors.error,
  },
  label: {
    fontSize: 12,
    fontWeight: "600",
    color: colors.onSurfaceVariant,
  },
  hashtagRow: {
    flexDirection: "row",
    flexWrap: "wrap",
    gap: 8,
    alignItems: "center",
  },
  hashtagChip: {
    flexDirection: "row",
    alignItems: "center",
    gap: 4,
    backgroundColor: colors.surfaceContainerHigh,
    borderRadius: 16,
    paddingHorizontal: 10,
    paddingVertical: 6,
  },
  hashtagText: {
    fontSize: 14,
    color: colors.onSurfaceVariant,
  },
  hashtagInput: {
    minWidth: 120,
    fontSize: 14,
    color: colors.onSurface,
    paddingVertical: 6,
  },
  visibilityBtn: {
    flexDirection: "row",
    alignItems: "center",
    gap: 8,
    borderWidth: 1,
    borderColor: colors.outlineVariant,
    borderRadius: 8,
    paddingHorizontal: 12,
    paddingVertical: 10,
    alignSelf: "flex-start",
  },
  visibilityText: {
    fontSize: 14,
    color: colors.onSurface,
  },
  visibilityMenu: {
    borderWidth: 1,
    borderColor: colors.outlineVariant,
    borderRadius: 8,
    overflow: "hidden",
    marginTop: 4,
  },
  visibilityOption: {
    paddingHorizontal: 12,
    paddingVertical: 12,
    backgroundColor: colors.surfaceContainerLowest,
  },
  visibilityOptionText: {
    fontSize: 14,
    color: colors.onSurface,
  },
  switchRow: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    paddingVertical: 8,
  },
  switchLabel: {
    fontSize: 14,
    color: colors.onSurface,
  },
});
