import { ActivityIndicator, Image, Pressable, StyleSheet, Text, View } from "react-native";
import { colors } from "../../../shared/theme/colors";

const DEFAULT_AVATAR = "https://i.pravatar.cc/80?img=11";

export function UserSuggestionCard({
  user,
  subtitle,
  followLabel,
  onPressProfile,
  onToggleFollow,
  isFollowLoading = false,
  followDisabled = false,
}) {
  const isFollowing = user.followStatus === "ACCEPTED" || user.followStatus === "PENDING";
  const isDisabled = isFollowLoading || followDisabled;

  return (
    <View style={styles.card}>
      <Pressable style={styles.profileArea} onPress={() => onPressProfile?.(user.userId)}>
        <Image
          source={{ uri: user.avatarUrl || DEFAULT_AVATAR }}
          style={styles.avatar}
        />
        <View style={styles.info}>
          <Text style={styles.name} numberOfLines={1}>
            {user.displayName}
          </Text>
          <Text style={styles.subtitle} numberOfLines={1}>
            {subtitle}
          </Text>
        </View>
      </Pressable>

      <Pressable
        style={[
          styles.followBtn,
          isFollowing && styles.followBtnSecondary,
          isDisabled && styles.followBtnDisabled,
        ]}
        onPress={() => onToggleFollow?.(user)}
        disabled={isDisabled}
      >
        {isFollowLoading ? (
          <ActivityIndicator size="small" color={isFollowing ? colors.primary : colors.onPrimary} />
        ) : (
          <Text style={[styles.followText, isFollowing && styles.followTextSecondary]}>
            {followLabel}
          </Text>
        )}
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    flexDirection: "row",
    alignItems: "center",
    gap: 12,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: colors.outlineVariant,
    backgroundColor: colors.surfaceContainerLowest,
    padding: 12,
    marginHorizontal: 16,
    marginBottom: 12,
  },
  profileArea: {
    flex: 1,
    flexDirection: "row",
    alignItems: "center",
    gap: 12,
    minWidth: 0,
  },
  avatar: {
    width: 44,
    height: 44,
    borderRadius: 22,
  },
  info: {
    flex: 1,
    minWidth: 0,
  },
  name: {
    fontSize: 15,
    fontWeight: "600",
    color: colors.onSurface,
  },
  subtitle: {
    fontSize: 12,
    color: colors.onSurfaceVariant,
    marginTop: 2,
  },
  followBtn: {
    minWidth: 88,
    minHeight: 36,
    borderRadius: 18,
    backgroundColor: colors.primary,
    alignItems: "center",
    justifyContent: "center",
    paddingHorizontal: 12,
  },
  followBtnSecondary: {
    backgroundColor: colors.surfaceContainerLowest,
    borderWidth: 1,
    borderColor: colors.outlineVariant,
  },
  followBtnDisabled: {
    opacity: 0.6,
  },
  followText: {
    fontSize: 12,
    fontWeight: "600",
    color: colors.onPrimary,
  },
  followTextSecondary: {
    color: colors.onSurfaceVariant,
  },
});
