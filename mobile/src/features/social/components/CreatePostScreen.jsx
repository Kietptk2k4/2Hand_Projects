import { useCallback, useEffect } from "react";
import {
  ActivityIndicator,
  KeyboardAvoidingView,
  Platform,
  Pressable,
  StyleSheet,
  Text,
  View,
} from "react-native";
import { router } from "expo-router";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useSocialToast } from "../../../shared/components/SocialToastProvider";
import { ROUTES } from "../../../shared/constants/routes";
import { colors } from "../../../shared/theme/colors";
import { useCreatePost } from "../hooks/useCreatePost";
import { PostComposerForm } from "./PostComposerForm";

export function CreatePostScreen({ openPickerOnMount = false }) {
  const insets = useSafeAreaInsets();
  const { showToast } = useSocialToast();

  const onSuccess = useCallback(
    (created, { publish }) => {
      showToast(publish ? "Đăng bài thành công." : "Đã lưu bản nháp.");
      if (created?.postId) {
        router.replace(ROUTES.postDetail(created.postId));
      } else {
        router.replace(ROUTES.feed);
      }
    },
    [showToast]
  );

  const form = useCreatePost({ onSuccess });
  const isBusy = form.isSubmitting || form.isUploadingMedia;

  useEffect(() => {
    if (!openPickerOnMount) return undefined;
    const timer = setTimeout(() => {
      form.pickAndAddMedia();
    }, 300);
    return () => clearTimeout(timer);
  }, [openPickerOnMount]);

  const handlePublish = () => form.submit(true);
  const handleSaveDraft = () => form.submit(false);

  return (
    <KeyboardAvoidingView
      style={styles.root}
      behavior={Platform.OS === "ios" ? "padding" : undefined}
      keyboardVerticalOffset={insets.top + 56}
    >
      <View style={[styles.header, { paddingTop: insets.top }]}>
        <Pressable onPress={() => router.back()} style={styles.headerBtn}>
          <Text style={styles.cancelText}>Hủy</Text>
        </Pressable>
        <Text style={styles.title}>Tạo bài viết</Text>
        <View style={styles.headerActions}>
          <Pressable
            onPress={handleSaveDraft}
            disabled={isBusy}
            style={[styles.draftBtn, isBusy && styles.disabled]}
          >
            <Text style={styles.draftText}>
              {form.isSubmitting ? "…" : "Lưu nháp"}
            </Text>
          </Pressable>
          <Pressable
            onPress={handlePublish}
            disabled={isBusy}
            style={[styles.publishBtn, isBusy && styles.disabled]}
          >
            {form.isSubmitting ? (
              <ActivityIndicator size="small" color={colors.onPrimary} />
            ) : (
              <Text style={styles.publishText}>Đăng bài</Text>
            )}
          </Pressable>
        </View>
      </View>

      <PostComposerForm mode="create" form={form} isBusy={isBusy} />
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
    backgroundColor: colors.surface,
  },
  header: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    paddingHorizontal: 12,
    paddingBottom: 8,
    borderBottomWidth: 1,
    borderBottomColor: colors.outlineVariant,
    backgroundColor: colors.surfaceContainerLowest,
  },
  headerBtn: {
    minWidth: 48,
    minHeight: 44,
    justifyContent: "center",
  },
  cancelText: {
    fontSize: 16,
    color: colors.onSurfaceVariant,
  },
  title: {
    fontSize: 17,
    fontWeight: "600",
    color: colors.onSurface,
  },
  headerActions: {
    flexDirection: "row",
    alignItems: "center",
    gap: 8,
  },
  draftBtn: {
    paddingHorizontal: 10,
    paddingVertical: 8,
    borderRadius: 16,
    backgroundColor: colors.surfaceContainerLow,
  },
  draftText: {
    fontSize: 13,
    fontWeight: "600",
    color: colors.primary,
  },
  publishBtn: {
    paddingHorizontal: 14,
    paddingVertical: 8,
    borderRadius: 16,
    backgroundColor: colors.primary,
    minWidth: 88,
    alignItems: "center",
  },
  publishText: {
    fontSize: 13,
    fontWeight: "600",
    color: colors.onPrimary,
  },
  disabled: {
    opacity: 0.5,
  },
});
