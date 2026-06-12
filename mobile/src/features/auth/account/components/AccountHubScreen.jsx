import { router } from "expo-router";
import { useCallback, useMemo, useState } from "react";
import { Image, Pressable, ScrollView, StyleSheet, Text, View } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useLogout } from "../../hooks/useLogout";
import { useSocialToast } from "../../../../shared/components/SocialToastProvider";
import { resolveDevMediaUrl } from "../../../../shared/utils/resolveDevMediaUrl";
import { useThemeColors } from "../../../../shared/theme/useThemeColors";
import { ACCOUNT_HUB_LOGOUT_ITEM, ACCOUNT_HUB_MENU_ITEMS } from "../constants/accountMenuItems";
import { useAccountProfile } from "../hooks/useAccountProfile";
import { AccountConfirmModal } from "./AccountConfirmModal";
import { AccountInfoSkeleton } from "./AccountInfoSkeleton";
import { AccountMenuRow } from "./AccountMenuRow";

const DEFAULT_AVATAR = "https://i.pravatar.cc/200?img=11";

export function AccountHubScreen() {
  const colors = useThemeColors();
  const insets = useSafeAreaInsets();
  const { showToast } = useSocialToast();
  const [isLogoutModalOpen, setIsLogoutModalOpen] = useState(false);
  const accountProfile = useAccountProfile();

  const onFallbackMessage = useCallback(
    (message) => {
      showToast(message, "info");
    },
    [showToast]
  );

  const { performLogout, isLoggingOut } = useLogout({ onFallbackMessage });

  const openLogoutModal = useCallback(() => {
    if (isLoggingOut) return;
    setIsLogoutModalOpen(true);
  }, [isLoggingOut]);

  const closeLogoutModal = useCallback(() => {
    if (isLoggingOut) return;
    setIsLogoutModalOpen(false);
  }, [isLoggingOut]);

  const confirmLogout = useCallback(() => {
    performLogout();
  }, [performLogout]);

  const displayName = accountProfile.userProfile?.display_name || "Tài khoản 2Hands";
  const avatarUrl = resolveDevMediaUrl(
    accountProfile.userProfile?.avatar_url || DEFAULT_AVATAR
  );

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
        introCard: {
          backgroundColor: colors.surfaceContainerLowest,
          borderRadius: 16,
          padding: 16,
          borderWidth: 1,
          borderColor: colors.outlineVariant,
          minHeight: 120,
          justifyContent: "center",
        },
        profileHeader: {
          flexDirection: "row",
          alignItems: "center",
          gap: 16,
        },
        avatar: {
          width: 72,
          height: 72,
          borderRadius: 36,
          backgroundColor: colors.surfaceContainerHigh,
        },
        profileCopy: {
          flex: 1,
          gap: 6,
        },
        introTitle: {
          fontSize: 18,
          fontWeight: "600",
          color: colors.onSurface,
        },
        introSubtitle: {
          fontSize: 14,
          lineHeight: 20,
          color: colors.onSurfaceVariant,
        },
        errorBlock: {
          alignItems: "center",
          gap: 12,
        },
        errorText: {
          fontSize: 14,
          color: colors.onSurfaceVariant,
          textAlign: "center",
        },
        retryButton: {
          backgroundColor: colors.primary,
          borderRadius: 8,
          minHeight: 44,
          paddingHorizontal: 20,
          alignItems: "center",
          justifyContent: "center",
        },
        retryButtonText: {
          color: colors.onPrimary,
          fontSize: 14,
          fontWeight: "600",
        },
        menuCard: {
          backgroundColor: colors.surfaceContainerLowest,
          borderRadius: 16,
          borderWidth: 1,
          borderColor: colors.outlineVariant,
          overflow: "hidden",
        },
        divider: {
          height: 1,
          backgroundColor: colors.outlineVariant,
          marginLeft: 68,
        },
      }),
    [colors]
  );

  return (
    <>
      <ScrollView
        style={styles.screen}
        contentContainerStyle={[styles.content, { paddingBottom: insets.bottom + 24 }]}
      >
        <View style={styles.introCard}>
          {accountProfile.isLoading ? (
            <AccountInfoSkeleton />
          ) : accountProfile.isError || accountProfile.isEmpty ? (
            <View style={styles.errorBlock}>
              <Text style={styles.errorText}>{accountProfile.errorMessage}</Text>
              <Pressable style={styles.retryButton} onPress={accountProfile.retry}>
                <Text style={styles.retryButtonText}>Thử lại</Text>
              </Pressable>
            </View>
          ) : (
            <>
              <View style={styles.profileHeader}>
                <Image source={{ uri: avatarUrl }} style={styles.avatar} />
                <View style={styles.profileCopy}>
                  <Text style={styles.introTitle}>{displayName}</Text>
                  <Text style={styles.introSubtitle}>
                    Quản lý hồ sơ, quyền riêng tư và cài đặt ứng dụng.
                  </Text>
                </View>
              </View>
            </>
          )}
        </View>

        <View style={styles.menuCard}>
          {ACCOUNT_HUB_MENU_ITEMS.map((item, index) => (
            <View key={item.id}>
              {index > 0 ? <View style={styles.divider} /> : null}
              <AccountMenuRow
                label={item.label}
                icon={item.icon}
                danger={item.danger}
                onPress={() => router.push(item.route)}
              />
            </View>
          ))}

          <View style={styles.divider} />
          <AccountMenuRow
            label={ACCOUNT_HUB_LOGOUT_ITEM.label}
            icon={ACCOUNT_HUB_LOGOUT_ITEM.icon}
            showChevron={false}
            onPress={openLogoutModal}
          />
        </View>

      </ScrollView>

      <AccountConfirmModal
        visible={isLogoutModalOpen}
        title="Xác nhận đăng xuất"
        message="Bạn có chắc chắn muốn đăng xuất khỏi thiết bị này?"
        confirmLabel={isLoggingOut ? "Đang đăng xuất..." : "Đăng xuất"}
        onConfirm={confirmLogout}
        onCancel={closeLogoutModal}
        isLoading={isLoggingOut}
        danger
        icon="🚪"
      />
    </>
  );
}