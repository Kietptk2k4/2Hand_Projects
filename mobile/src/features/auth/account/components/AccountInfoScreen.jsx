import { router } from "expo-router";
import { useMemo } from "react";
import {
  Image,
  Linking,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { ROUTES } from "../../../../shared/constants/routes";
import { useThemeColors } from "../../../../shared/theme/useThemeColors";
import { resolveDevMediaUrl } from "../../../../shared/utils/resolveDevMediaUrl";
import { APPEARANCE_LABELS, NOT_UPDATED } from "../constants/authUiStrings";
import { useAccountProfile } from "../hooks/useAccountProfile";
import { formatAccountDateTime } from "../utils/formatAccountDateTime";
import { AccountCard } from "./AccountCard";
import { AccountInfoRow } from "./AccountInfoRow";
import { AccountInfoSkeleton } from "./AccountInfoSkeleton";
import { AccountStatusBadge } from "./AccountStatusBadge";

function resolveText(value) {
  if (value === null || value === undefined || value === "") {
    return NOT_UPDATED;
  }
  return String(value);
}

function LinkAction({ label, onPress, linkStyle }) {
  return (
    <Pressable onPress={onPress} accessibilityRole="button">
      <Text style={linkStyle}>{label}</Text>
    </Pressable>
  );
}

export function AccountInfoScreen() {
  const colors = useThemeColors();
  const insets = useSafeAreaInsets();
  const {
    user,
    userProfile,
    settings,
    isLoading,
    isError,
    isEmpty,
    errorMessage,
    retry,
  } = useAccountProfile();

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
        profileHeader: {
          flexDirection: "row",
          alignItems: "center",
          gap: 16,
          marginBottom: 8,
        },
        avatar: {
          width: 64,
          height: 64,
          borderRadius: 32,
          borderWidth: 1,
          borderColor: colors.outlineVariant,
        },
        avatarPlaceholder: {
          backgroundColor: colors.surfaceContainerHigh,
        },
        profileHeaderText: {
          flex: 1,
          gap: 4,
        },
        displayName: {
          fontSize: 16,
          fontWeight: "600",
          color: colors.onSurface,
        },
        valueText: {
          fontSize: 14,
          color: colors.onSurface,
        },
        link: {
          fontSize: 14,
          fontWeight: "500",
          color: colors.primary,
        },
        socialList: {
          gap: 6,
        },
        actionsRow: {
          marginTop: 12,
          flexDirection: "row",
          flexWrap: "wrap",
          gap: 16,
        },
        securityHint: {
          fontSize: 14,
          lineHeight: 20,
          color: colors.onSurfaceVariant,
        },
        comingSoon: {
          marginTop: 12,
          fontSize: 14,
          fontWeight: "500",
          color: colors.outline,
        },
      }),
    [colors]
  );

  if (isLoading) {
    return (
      <ScrollView
        style={styles.screen}
        contentContainerStyle={[styles.content, { paddingBottom: insets.bottom + 24 }]}
      >
        <AccountInfoSkeleton />
      </ScrollView>
    );
  }

  if (isError || isEmpty) {
    return (
      <View style={[styles.centered, { paddingBottom: insets.bottom }]}>
        <Text style={styles.errorText}>{errorMessage}</Text>
        <Pressable style={styles.retryButton} onPress={retry}>
          <Text style={styles.retryButtonText}>Thử lại</Text>
        </Pressable>
      </View>
    );
  }

  const socialEntries = Object.entries(userProfile?.social_links || {});
  const username = user?.username ?? userProfile?.username ?? null;
  const createdAt = user?.created_at ?? user?.createdAt ?? null;

  return (
    <ScrollView
      style={styles.screen}
      contentContainerStyle={[styles.content, { paddingBottom: insets.bottom + 24 }]}
    >
      <View style={styles.header}>
        <Text style={styles.headerTitle}>Thông tin tài khoản</Text>
        <Text style={styles.headerSubtitle}>
          Xem thông tin tài khoản, hồ sơ và cài đặt tóm tắt.
        </Text>
      </View>

      <AccountCard title="Tài khoản">
        <AccountInfoRow label="Họ tên" value={userProfile?.display_name} />
        <AccountInfoRow label="Tên người dùng" value={username} />
        <AccountInfoRow label="Email" value={user?.email} />
        <AccountInfoRow label="Trạng thái">
          <AccountStatusBadge status={user?.status} />
        </AccountInfoRow>
        <AccountInfoRow label="Email đã xác thực">
          <Text style={styles.valueText}>
            {user?.email_verified ? "Đã xác thực" : "Chưa xác thực"}
          </Text>
        </AccountInfoRow>
        <AccountInfoRow label="Số điện thoại" value={user?.phone} />
        <AccountInfoRow
          label="Ngày tạo tài khoản"
          value={createdAt ? formatAccountDateTime(createdAt) : null}
        />
        <AccountInfoRow
          label="Lần đăng nhập gần nhất"
          value={user?.last_login_at ? formatAccountDateTime(user.last_login_at) : null}
        />
      </AccountCard>

      <AccountCard title="Hồ sơ">
        <View style={styles.profileHeader}>
          {userProfile?.avatar_url ? (
            <Image
              source={{ uri: resolveDevMediaUrl(userProfile.avatar_url) }}
              style={styles.avatar}
              accessibilityIgnoresInvertColors
            />
          ) : (
            <View style={[styles.avatar, styles.avatarPlaceholder]} />
          )}
          <View style={styles.profileHeaderText}>
            <Text style={styles.displayName}>{resolveText(userProfile?.display_name)}</Text>
            <LinkAction
              label="Chỉnh sửa hồ sơ"
              linkStyle={styles.link}
              onPress={() => router.push(ROUTES.accountEdit)}
            />
          </View>
        </View>
        <AccountInfoRow label="Giới thiệu" value={userProfile?.bio} />
        <AccountInfoRow label="Website">
          {userProfile?.website ? (
            <Pressable onPress={() => Linking.openURL(userProfile.website)}>
              <Text style={styles.link}>{userProfile.website}</Text>
            </Pressable>
          ) : (
            <Text style={styles.valueText}>{NOT_UPDATED}</Text>
          )}
        </AccountInfoRow>
        <AccountInfoRow label="Mạng xã hội">
          {socialEntries.length > 0 ? (
            <View style={styles.socialList}>
              {socialEntries.map(([key, url]) => (
                <Pressable key={key} onPress={() => Linking.openURL(url)}>
                  <Text style={styles.link}>
                    {key}: {url}
                  </Text>
                </Pressable>
              ))}
            </View>
          ) : (
            <Text style={styles.valueText}>{NOT_UPDATED}</Text>
          )}
        </AccountInfoRow>
        <AccountInfoRow label="Chế độ riêng tư">
          <Text style={styles.valueText}>
            {userProfile?.is_private ? "Riêng tư" : "Công khai"}
          </Text>
        </AccountInfoRow>
      </AccountCard>

      <AccountCard title="Cài đặt (tóm tắt)">
        <AccountInfoRow
          label="Giao diện"
          value={
            APPEARANCE_LABELS[settings?.appearance_mode] ||
            settings?.appearance_mode ||
            null
          }
        />
        <View style={styles.actionsRow}>
          <LinkAction
            label="Cập nhật cài đặt"
            linkStyle={styles.link}
            onPress={() => router.push(ROUTES.accountSettings)}
          />
        </View>
      </AccountCard>

      <AccountCard title="Bảo mật">
        <Text style={styles.securityHint}>
          Xem phiên đăng nhập đang hoạt động và lịch sử đăng nhập của tài khoản.
        </Text>
        <Text style={styles.comingSoon}>Tính năng đang được phát triển.</Text>
      </AccountCard>
    </ScrollView>
  );
}