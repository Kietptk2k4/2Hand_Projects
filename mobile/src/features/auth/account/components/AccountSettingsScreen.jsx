import { useCallback, useMemo } from "react";
import {
  ActivityIndicator,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useSocialToast } from "../../../../shared/components/SocialToastProvider";
import { useThemeColors } from "../../../../shared/theme/useThemeColors";
import { useAccountSettings } from "../hooks/useAccountSettings";
import { AccountCard } from "./AccountCard";
import { AccountInfoSkeleton } from "./AccountInfoSkeleton";
import { AppearanceModePicker } from "./AppearanceModePicker";

export function AccountSettingsScreen() {
  const colors = useThemeColors();
  const insets = useSafeAreaInsets();
  const { showToast } = useSocialToast();

  const styles = useMemo(
    () =>
      StyleSheet.create({
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
        formError: {
          marginTop: 16,
          fontSize: 14,
          color: colors.error,
        },
        actions: {
          marginTop: 24,
          paddingTop: 16,
          borderTopWidth: 1,
          borderTopColor: colors.outlineVariant,
          alignItems: "flex-end",
        },
        primaryButton: {
          minHeight: 48,
          minWidth: 140,
          borderRadius: 8,
          backgroundColor: colors.primary,
          alignItems: "center",
          justifyContent: "center",
          paddingHorizontal: 16,
        },
        primaryButtonDisabled: {
          opacity: 0.7,
        },
        buttonPressed: {
          opacity: 0.9,
        },
        primaryButtonText: {
          color: colors.onPrimary,
          fontSize: 14,
          fontWeight: "600",
        },
      }),
    [colors]
  );

  const onSuccess = useCallback(() => {
    showToast("Cập nhật cài đặt thành công.");
  }, [showToast]);

  const onError = useCallback(
    (message) => {
      showToast(message, "error");
    },
    [showToast]
  );

  const settings = useAccountSettings({ onSuccess, onError });
  const canSave = settings.isDirty && !settings.isSubmitting;

  if (settings.isLoading) {
    return (
      <ScrollView
        style={styles.screen}
        contentContainerStyle={[styles.content, { paddingBottom: insets.bottom + 24 }]}
      >
        <AccountInfoSkeleton />
      </ScrollView>
    );
  }

  if (settings.isProfileError) {
    return (
      <View style={[styles.centered, { paddingBottom: insets.bottom }]}>
        <Text style={styles.errorText}>{settings.profileErrorMessage}</Text>
        <Pressable style={styles.retryButton} onPress={settings.retryProfile}>
          <Text style={styles.retryButtonText}>Thử lại</Text>
        </Pressable>
      </View>
    );
  }

  return (
    <ScrollView
      style={styles.screen}
      contentContainerStyle={[styles.content, { paddingBottom: insets.bottom + 24 }]}
    >
      <View style={styles.header}>
        <Text style={styles.headerTitle}>Cài đặt</Text>
        <Text style={styles.headerSubtitle}>
          Tùy chỉnh giao diện hiển thị của ứng dụng 2Hands.
        </Text>
      </View>

      <AccountCard title="Giao diện">
        <AppearanceModePicker
          value={settings.appearanceMode}
          onChange={settings.setAppearanceMode}
          disabled={settings.isSubmitting}
        />

        {settings.errorMessage ? (
          <Text style={styles.formError}>{settings.errorMessage}</Text>
        ) : null}

        <View style={styles.actions}>
          <Pressable
            onPress={settings.submit}
            disabled={!canSave}
            style={({ pressed }) => [
              styles.primaryButton,
              !canSave && styles.primaryButtonDisabled,
              pressed && canSave && styles.buttonPressed,
            ]}
          >
            {settings.isSubmitting ? (
              <ActivityIndicator color={colors.onPrimary} />
            ) : (
              <Text style={styles.primaryButtonText}>Lưu cài đặt</Text>
            )}
          </Pressable>
        </View>
      </AccountCard>
    </ScrollView>
  );
}
