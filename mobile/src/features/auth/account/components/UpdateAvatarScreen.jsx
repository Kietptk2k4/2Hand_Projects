import { useCallback } from "react";
import { Ionicons } from "@expo/vector-icons";
import {
  ActivityIndicator,
  Image,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useSocialToast } from "../../../../shared/components/SocialToastProvider";
import { colors } from "../../../../shared/theme/colors";
import { useUpdateAvatar } from "../hooks/useUpdateAvatar";
import { AccountCard } from "./AccountCard";
import { AccountInfoSkeleton } from "./AccountInfoSkeleton";

const GUIDELINES = [
  "Sử dụng hình ảnh rõ nét, chụp chính diện khuôn mặt.",
  "Định dạng: JPG, PNG, WEBP.",
  "Tối đa 5MB. Độ phân giải khuyến dùng 500x500px.",
];

export function UpdateAvatarScreen() {
  const insets = useSafeAreaInsets();
  const { showToast } = useSocialToast();

  const onSuccess = useCallback(() => {
    showToast("Cập nhật ảnh đại diện thành công.");
  }, [showToast]);

  const onError = useCallback(
    (message) => {
      showToast(message, "error");
    },
    [showToast]
  );

  const avatar = useUpdateAvatar({ onSuccess, onError });

  if (avatar.isLoading) {
    return (
      <ScrollView
        style={styles.screen}
        contentContainerStyle={[styles.content, { paddingBottom: insets.bottom + 24 }]}
      >
        <AccountInfoSkeleton />
      </ScrollView>
    );
  }

  if (avatar.isProfileError) {
    return (
      <View style={[styles.centered, { paddingBottom: insets.bottom }]}>
        <Text style={styles.errorText}>{avatar.profileErrorMessage}</Text>
        <Pressable style={styles.retryButton} onPress={avatar.retryProfile}>
          <Text style={styles.retryButtonText}>Thử lại</Text>
        </Pressable>
      </View>
    );
  }

  const showProgress = avatar.uploadProgress !== null;
  const isUploading = avatar.isUploading;
  const previewOpacity = showProgress && avatar.uploadProgress < 100 ? 0.5 : 1;

  return (
    <ScrollView
      style={styles.screen}
      contentContainerStyle={[styles.content, { paddingBottom: insets.bottom + 24 }]}
    >
      <View style={styles.header}>
        <Text style={styles.headerTitle}>Cập nhật ảnh đại diện</Text>
        <Text style={styles.headerSubtitle}>
          Chọn một bức ảnh thể hiện sự chuyên nghiệp của bạn.
        </Text>
      </View>

      <AccountCard>
        <View style={styles.previewSection}>
          <Pressable
            onPress={avatar.pickImage}
            disabled={isUploading}
            accessibilityRole="button"
            accessibilityLabel="Chọn ảnh đại diện"
            style={({ pressed }) => [styles.previewButton, pressed && styles.previewPressed]}
          >
            <View style={styles.previewRing}>
              {avatar.previewUri ? (
                <Image
                  source={{ uri: avatar.previewUri }}
                  style={[styles.previewImage, { opacity: previewOpacity }]}
                  accessibilityIgnoresInvertColors
                />
              ) : (
                <View style={[styles.previewImage, styles.previewPlaceholder]}>
                  <Ionicons name="person" size={64} color={colors.outline} />
                </View>
              )}
              <View style={styles.previewOverlay}>
                <Ionicons name="camera" size={32} color={colors.onPrimary} />
              </View>
            </View>
          </Pressable>
          <Text style={styles.previewHint}>Nhấn vào ảnh để thay đổi</Text>
          {avatar.selectedAsset ? (
            <Pressable onPress={avatar.pickImage} disabled={isUploading}>
              <Text style={styles.replaceLink}>Chọn ảnh khác</Text>
            </Pressable>
          ) : null}
        </View>

        <View style={styles.guideSection}>
          <Text style={styles.guideTitle}>Hướng dẫn tải lên</Text>
          {GUIDELINES.map((line) => (
            <Text key={line} style={styles.guideItem}>
              • {line}
            </Text>
          ))}
        </View>

        {showProgress ? (
          <View style={styles.progressCard}>
            <View style={styles.progressHeader}>
              <Text style={styles.progressLabel}>
                {avatar.uploadProgress >= 100 ? "Hoàn tất" : "Đang tải lên..."}
              </Text>
              <Text style={styles.progressPercent}>{avatar.uploadProgress}%</Text>
            </View>
            <View style={styles.progressTrack}>
              <View style={[styles.progressFill, { width: `${avatar.uploadProgress}%` }]} />
            </View>
          </View>
        ) : null}

        {avatar.errorMessage ? (
          <Text style={styles.formError}>{avatar.errorMessage}</Text>
        ) : null}

        <View style={styles.actions}>
          <Pressable
            onPress={avatar.resetSelection}
            disabled={isUploading}
            style={({ pressed }) => [styles.secondaryButton, pressed && styles.buttonPressed]}
          >
            <Text style={styles.secondaryButtonText}>Hủy</Text>
          </Pressable>
          <Pressable
            onPress={avatar.submitUpload}
            disabled={!avatar.selectedAsset || isUploading}
            style={({ pressed }) => [
              styles.primaryButton,
              (!avatar.selectedAsset || isUploading) && styles.primaryButtonDisabled,
              pressed && styles.buttonPressed,
            ]}
          >
            {isUploading ? (
              <ActivityIndicator color={colors.onPrimary} />
            ) : (
              <Text style={styles.primaryButtonText}>Cập nhật ảnh đại diện</Text>
            )}
          </Pressable>
        </View>
      </AccountCard>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  screen: {
    flex: 1,
    backgroundColor: colors.surface,
  },
  content: {
    paddingHorizontal: 16,
    paddingTop: 16,
    gap: 16,
  },
  header: {
    gap: 8,
  },
  headerTitle: {
    fontSize: 20,
    fontWeight: "600",
    color: colors.onSurface,
  },
  headerSubtitle: {
    fontSize: 14,
    lineHeight: 20,
    color: colors.onSurfaceVariant,
  },
  centered: {
    flex: 1,
    backgroundColor: colors.surface,
    alignItems: "center",
    justifyContent: "center",
    paddingHorizontal: 24,
    gap: 16,
  },
  errorText: {
    fontSize: 16,
    color: colors.onSurfaceVariant,
    textAlign: "center",
  },
  retryButton: {
    backgroundColor: colors.primary,
    borderRadius: 8,
    minHeight: 48,
    minWidth: 120,
    paddingHorizontal: 20,
    alignItems: "center",
    justifyContent: "center",
  },
  retryButtonText: {
    color: colors.onPrimary,
    fontSize: 14,
    fontWeight: "600",
  },
  previewSection: {
    alignItems: "center",
    marginBottom: 24,
    gap: 8,
  },
  previewButton: {
    alignItems: "center",
  },
  previewPressed: {
    opacity: 0.95,
  },
  previewRing: {
    width: 192,
    height: 192,
    borderRadius: 96,
    borderWidth: 4,
    borderColor: colors.surfaceContainerLowest,
    overflow: "hidden",
    backgroundColor: colors.surfaceContainerHigh,
    position: "relative",
  },
  previewImage: {
    width: "100%",
    height: "100%",
  },
  previewPlaceholder: {
    alignItems: "center",
    justifyContent: "center",
    backgroundColor: colors.surfaceContainerHigh,
  },
  previewOverlay: {
    ...StyleSheet.absoluteFillObject,
    alignItems: "center",
    justifyContent: "center",
    backgroundColor: "rgba(0,0,0,0.2)",
  },
  previewHint: {
    marginTop: 8,
    fontSize: 12,
    fontWeight: "600",
    color: colors.onSurfaceVariant,
    textAlign: "center",
  },
  replaceLink: {
    fontSize: 14,
    fontWeight: "500",
    color: colors.primary,
  },
  guideSection: {
    gap: 8,
    marginBottom: 16,
  },
  guideTitle: {
    fontSize: 16,
    fontWeight: "600",
    color: colors.onSurface,
  },
  guideItem: {
    fontSize: 14,
    lineHeight: 20,
    color: colors.onSurfaceVariant,
  },
  progressCard: {
    borderWidth: 1,
    borderColor: colors.outlineVariant,
    borderRadius: 12,
    padding: 16,
    backgroundColor: colors.surfaceContainerLow,
    marginBottom: 12,
  },
  progressHeader: {
    flexDirection: "row",
    justifyContent: "space-between",
    marginBottom: 8,
  },
  progressLabel: {
    fontSize: 12,
    fontWeight: "600",
    color: colors.primary,
  },
  progressPercent: {
    fontSize: 12,
    fontWeight: "600",
    color: colors.onSurfaceVariant,
  },
  progressTrack: {
    height: 8,
    borderRadius: 999,
    backgroundColor: colors.outlineVariant,
    overflow: "hidden",
  },
  progressFill: {
    height: "100%",
    borderRadius: 999,
    backgroundColor: colors.secondary,
  },
  formError: {
    fontSize: 14,
    color: colors.error,
    marginBottom: 8,
  },
  actions: {
    flexDirection: "row",
    justifyContent: "flex-end",
    gap: 12,
    marginTop: 8,
    paddingTop: 16,
    borderTopWidth: 1,
    borderTopColor: colors.outlineVariant,
  },
  primaryButton: {
    minHeight: 48,
    minWidth: 168,
    borderRadius: 8,
    backgroundColor: colors.primary,
    alignItems: "center",
    justifyContent: "center",
    paddingHorizontal: 16,
  },
  primaryButtonDisabled: {
    opacity: 0.7,
  },
  secondaryButton: {
    minHeight: 48,
    minWidth: 88,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: colors.outlineVariant,
    alignItems: "center",
    justifyContent: "center",
    paddingHorizontal: 16,
    backgroundColor: colors.surfaceContainerLowest,
  },
  buttonPressed: {
    opacity: 0.9,
  },
  primaryButtonText: {
    color: colors.onPrimary,
    fontSize: 14,
    fontWeight: "600",
    textAlign: "center",
  },
  secondaryButtonText: {
    color: colors.onSurface,
    fontSize: 14,
    fontWeight: "600",
  },
});