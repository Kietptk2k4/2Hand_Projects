import { useCallback } from "react";
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
import { colors } from "../../../shared/theme/colors";
import { useEditPost } from "../hooks/useEditPost";
import { PostComposerForm } from "./PostComposerForm";

export function EditPostScreen({ postId }) {
  const insets = useSafeAreaInsets();
  const { showToast } = useSocialToast();

  const onSuccess = useCallback(() => {
    showToast("Cập nhật bài viết thành công.");
    router.back();
  }, [showToast]);

  const form = useEditPost({ postId, onSuccess });
  const isBusy = form.isSubmitting || form.isUploadingMedia || form.isLoadingInitial;

  if (form.isLoadError && !form.isLoadingInitial) {
    return (
      <View style={styles.centered}>
        <Text style={styles.errorText}>{form.loadError}</Text>
        {!form.isUnauthorized ? (
          <Pressable style={styles.retryBtn} onPress={form.retryLoad}>
            <Text style={styles.retryText}>Thử lại</Text>
          </Pressable>
        ) : (
          <Pressable style={styles.retryBtn} onPress={() => router.back()}>
            <Text style={styles.retryText}>Quay lại</Text>
          </Pressable>
        )}
      </View>
    );
  }

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
        <Text style={styles.title}>Chỉnh sửa bài viết</Text>
        <Pressable
          onPress={form.submitUpdate}
          disabled={isBusy || form.isLoadError}
          style={[styles.saveBtn, isBusy && styles.disabled]}
        >
          {form.isSubmitting ? (
            <ActivityIndicator size="small" color={colors.onPrimary} />
          ) : (
            <Text style={styles.saveText}>Cập nhật</Text>
          )}
        </Pressable>
      </View>

      {form.loadError && !form.isLoadError ? (
        <View style={styles.bannerError}>
          <Text style={styles.bannerErrorText}>{form.loadError}</Text>
        </View>
      ) : null}

      <PostComposerForm
        mode="edit"
        form={form}
        isBusy={isBusy}
        isLoadingInitial={form.isLoadingInitial}
      />
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
  saveBtn: {
    paddingHorizontal: 14,
    paddingVertical: 8,
    borderRadius: 16,
    backgroundColor: colors.primary,
    minWidth: 88,
    alignItems: "center",
  },
  saveText: {
    fontSize: 13,
    fontWeight: "600",
    color: colors.onPrimary,
  },
  disabled: {
    opacity: 0.5,
  },
  centered: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
    padding: 24,
    backgroundColor: colors.surface,
    gap: 16,
  },
  errorText: {
    fontSize: 14,
    color: colors.onErrorContainer,
    textAlign: "center",
  },
  retryBtn: {
    backgroundColor: colors.primary,
    borderRadius: 8,
    paddingHorizontal: 20,
    paddingVertical: 12,
  },
  retryText: {
    color: colors.onPrimary,
    fontWeight: "600",
  },
  bannerError: {
    backgroundColor: colors.errorContainer,
    padding: 12,
  },
  bannerErrorText: {
    color: colors.onErrorContainer,
    fontSize: 14,
  },
});
