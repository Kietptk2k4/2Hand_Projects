import { useCallback } from "react";
import { Pressable, ScrollView, StyleSheet, Text, View } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useSocialToast } from "../../../../shared/components/SocialToastProvider";
import { colors } from "../../../../shared/theme/colors";
import { usePrivacySettings } from "../hooks/usePrivacySettings";
import { AccountCard } from "./AccountCard";
import { AccountInfoSkeleton } from "./AccountInfoSkeleton";
import { AccountSettingSwitch } from "./AccountSettingSwitch";

export function PrivacyScreen() {
  const insets = useSafeAreaInsets();
  const { showToast } = useSocialToast();

  const onSuccess = useCallback(
    (nextValue) => {
      showToast(
        nextValue ? "Đã bật chế độ riêng tư." : "Đã tắt chế độ riêng tư."
      );
    },
    [showToast]
  );

  const onError = useCallback(
    (message) => {
      showToast(message, "error");
    },
    [showToast]
  );

  const privacy = usePrivacySettings({ onSuccess, onError });

  if (privacy.isLoading) {
    return (
      <ScrollView
        style={styles.screen}
        contentContainerStyle={[styles.content, { paddingBottom: insets.bottom + 24 }]}
      >
        <AccountInfoSkeleton />
      </ScrollView>
    );
  }

  if (privacy.isProfileError) {
    return (
      <View style={[styles.centered, { paddingBottom: insets.bottom }]}>
        <Text style={styles.errorText}>{privacy.profileErrorMessage}</Text>
        <Pressable style={styles.retryButton} onPress={privacy.retryProfile}>
          <Text style={styles.retryButtonText}>Thử lại</Text>
        </Pressable>
      </View>
    );
  }

  const privateDescription = privacy.isPrivate
    ? "Chỉ hiển thị tên và ảnh đại diện với người khác."
    : "Hồ sơ công khai — mọi người xem được bio, website và liên kết.";

  return (
    <ScrollView
      style={styles.screen}
      contentContainerStyle={[styles.content, { paddingBottom: insets.bottom + 24 }]}
    >
      <View style={styles.header}>
        <Text style={styles.headerTitle}>Quyền riêng tư</Text>
        <Text style={styles.headerSubtitle}>
          Kiểm soát khả năng hiển thị hồ sơ của bạn với cộng đồng.
        </Text>
      </View>

      <AccountCard>
        <AccountSettingSwitch
          title="Chế độ hồ sơ riêng tư"
          description={privateDescription}
          value={privacy.isPrivate}
          onValueChange={privacy.toggle}
          isSaving={privacy.isSaving}
        />

        <View style={styles.infoBox}>
          <Text style={styles.infoIcon}>ℹ️</Text>
          <Text style={styles.infoText}>
            Khi bật chế độ riêng tư, người xem công khai chỉ thấy tên hiển thị và ảnh đại
            diện. Bio, website và mạng xã hội sẽ bị ẩn.
          </Text>
        </View>

        {privacy.errorMessage ? (
          <Text style={styles.formError}>{privacy.errorMessage}</Text>
        ) : null}
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
  infoBox: {
    marginTop: 24,
    flexDirection: "row",
    alignItems: "flex-start",
    gap: 12,
    borderRadius: 12,
    backgroundColor: colors.surfaceContainerLow,
    padding: 16,
  },
  infoIcon: {
    fontSize: 16,
    lineHeight: 20,
  },
  infoText: {
    flex: 1,
    fontSize: 14,
    lineHeight: 20,
    color: colors.onSurfaceVariant,
  },
  formError: {
    marginTop: 16,
    fontSize: 14,
    color: colors.error,
  },
});
