import { ActivityIndicator, Image, Pressable, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { router } from "expo-router";
import { useAccountProfile } from "../../auth/account/hooks/useAccountProfile";
import { ROUTES } from "../../../shared/constants/routes";
import { resolveDevMediaUrl } from "../../../shared/utils/resolveDevMediaUrl";
import { formatSocialCount } from "../utils/formatSocialCount";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

const DEFAULT_AVATAR = "https://i.pravatar.cc/96?img=11";

function StatValue({ value, isLoading, styles }) {
  if (isLoading) {
    return <View style={styles.statSkeleton} />;
  }
  return <Text style={styles.statValue}>{formatSocialCount(value) ?? "—"}</Text>;
}

function createStyles(colors) {
  return {
    card: {
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      padding: 16,
      gap: 12,
    },
    profileRow: {
      flexDirection: "row",
      alignItems: "center",
      gap: 12,
    },
    avatarRing: {
      borderRadius: 32,
      borderWidth: 2,
      borderColor: colors.primary,
      padding: 2,
    },
    avatar: {
      width: 56,
      height: 56,
      borderRadius: 28,
      backgroundColor: colors.surfaceContainerHigh,
    },
    avatarSkeleton: {
      width: 56,
      height: 56,
      borderRadius: 28,
      backgroundColor: colors.surfaceContainerHigh,
    },
    profileInfo: {
      flex: 1,
      minWidth: 0,
    },
    displayName: {
      fontSize: 17,
      fontWeight: "600",
      color: colors.onSurface,
    },
    bio: {
      fontSize: 13,
      color: colors.onSurfaceVariant,
      marginTop: 2,
    },
    statsRow: {
      flexDirection: "row",
      borderTopWidth: 1,
      borderTopColor: colors.outlineVariant,
      paddingTop: 12,
    },
    stat: {
      flex: 1,
      alignItems: "center",
      gap: 2,
    },
    statDivider: {
      width: 1,
      backgroundColor: colors.outlineVariant,
      marginHorizontal: 4,
    },
    statValue: {
      fontSize: 18,
      fontWeight: "600",
      color: colors.onSurface,
    },
    statLabel: {
      fontSize: 11,
      fontWeight: "600",
      color: colors.onSurfaceVariant,
    },
    statSkeleton: {
      width: 28,
      height: 20,
      borderRadius: 4,
      backgroundColor: colors.surfaceContainerHigh,
    },
    linksCard: {
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      padding: 16,
      gap: 4,
    },
    linksTitle: {
      fontSize: 12,
      fontWeight: "600",
      letterSpacing: 0.6,
      textTransform: "uppercase",
      color: colors.onSurface,
      marginBottom: 8,
    },
    linkRow: {
      flexDirection: "row",
      alignItems: "center",
      gap: 12,
      paddingVertical: 10,
      paddingHorizontal: 4,
      minHeight: 44,
    },
    linkLabel: {
      flex: 1,
      fontSize: 14,
      color: colors.onSurface,
    },
    linkBadge: {
      fontSize: 12,
      fontWeight: "600",
      color: colors.onSurfaceVariant,
    },
    linkBadgeSkeleton: {
      width: 20,
      height: 14,
      borderRadius: 4,
      backgroundColor: colors.surfaceContainerHigh,
    },
    viewProfileLink: {
      alignSelf: "center",
      paddingVertical: 4,
    },
    viewProfileText: {
      fontSize: 14,
      fontWeight: "500",
      color: colors.primary,
    },
  };
}

export function FeedProfileSummary({ userId, stats }) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);
  const { profile, isLoading: isProfileLoading } = useAccountProfile({
    enabled: Boolean(userId),
  });

  const displayName =
    profile?.profile?.display_name ||
    profile?.profile?.displayName ||
    profile?.email ||
    "Thành viên";
  const bio =
    profile?.profile?.bio || "Thành viên 2Hands";
  const avatarUrl = resolveDevMediaUrl(
    profile?.profile?.avatar_url || profile?.profile?.avatarUrl || DEFAULT_AVATAR
  );

  const { postCount, followerCount, savedCount, isLoading: isStatsLoading } = stats;

  const openSelfProfile = () => {
    if (!userId) return;
    router.push(ROUTES.userProfile(userId));
  };

  const openSaved = () => {
    router.push(ROUTES.saved);
  };

  return (
    <View style={{ gap: 12 }}>
      <View style={styles.card}>
        <Pressable style={styles.profileRow} onPress={openSelfProfile}>
          <View style={styles.avatarRing}>
            {isProfileLoading ? (
              <View style={styles.avatarSkeleton} />
            ) : (
              <Image source={{ uri: avatarUrl }} style={styles.avatar} />
            )}
          </View>
          <View style={styles.profileInfo}>
            <Text style={styles.displayName} numberOfLines={1}>
              {displayName}
            </Text>
            <Text style={styles.bio} numberOfLines={2}>
              {bio}
            </Text>
          </View>
        </Pressable>

        <View style={styles.statsRow}>
          <Pressable style={styles.stat} onPress={openSelfProfile}>
            <StatValue value={postCount} isLoading={isStatsLoading} styles={styles} />
            <Text style={styles.statLabel}>Bài viết</Text>
          </Pressable>
          <View style={styles.statDivider} />
          <Pressable style={styles.stat} onPress={openSelfProfile}>
            <StatValue value={followerCount} isLoading={isStatsLoading} styles={styles} />
            <Text style={styles.statLabel}>Người theo dõi</Text>
          </Pressable>
        </View>

        <Pressable style={styles.viewProfileLink} onPress={openSelfProfile}>
          <Text style={styles.viewProfileText}>Xem hồ sơ</Text>
        </Pressable>
      </View>

      <View style={styles.linksCard}>
        <Text style={styles.linksTitle}>Liên kết nhanh</Text>
        <Pressable style={styles.linkRow} onPress={openSaved}>
          <Ionicons name="bookmark" size={20} color={colors.primary} />
          <Text style={styles.linkLabel}>Đã lưu</Text>
          {isStatsLoading ? (
            <View style={styles.linkBadgeSkeleton} />
          ) : savedCount !== null ? (
            <Text style={styles.linkBadge}>{formatSocialCount(savedCount)}</Text>
          ) : null}
        </Pressable>
      </View>
    </View>
  );
}
